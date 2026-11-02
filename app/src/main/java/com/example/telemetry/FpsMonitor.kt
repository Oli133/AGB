package com.example.telemetry

import android.view.Choreographer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FpsStats(
    val currentFps: Float = 60.0f,
    val droppedFrames: Int = 0,
    val jitterMs: Float = 1.0f
)

class FpsMonitor {
    private val _fpsStats = MutableStateFlow(FpsStats())
    val fpsStats: StateFlow<FpsStats> = _fpsStats.asStateFlow()

    private var isRunning = false
    private var lastFrameTimeNanos = 0L
    private var frameCount = 0
    private var droppedCount = 0
    private var accumulatedJitter = 0.0
    private var windowStartTimeNanos = 0L

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isRunning) return

            if (lastFrameTimeNanos > 0) {
                val deltaNanos = frameTimeNanos - lastFrameTimeNanos
                val deltaMs = deltaNanos / 1_000_000.0f
                // Detect dropped frames (nominal ~16.6ms for 60Hz, ~11.1ms for 90Hz, ~8.3ms for 120Hz)
                if (deltaMs > 20.0f) {
                    val missed = ((deltaMs - 16.6f) / 16.6f).toInt()
                    droppedCount += missed.coerceAtLeast(1)
                }
                val diffFromNominal = kotlin.math.abs(deltaMs - 16.6f)
                accumulatedJitter += diffFromNominal
                frameCount++
            }

            if (windowStartTimeNanos == 0L) {
                windowStartTimeNanos = frameTimeNanos
            } else if (frameTimeNanos - windowStartTimeNanos >= 1_000_000_000L) { // 1 second window
                val elapsedSec = (frameTimeNanos - windowStartTimeNanos) / 1_000_000_000.0f
                val measuredFps = if (elapsedSec > 0) frameCount / elapsedSec else 60.0f
                val avgJitter = if (frameCount > 0) (accumulatedJitter / frameCount).toFloat() else 1.0f

                _fpsStats.value = FpsStats(
                    currentFps = measuredFps.coerceIn(24.0f, 144.0f),
                    droppedFrames = droppedCount,
                    jitterMs = avgJitter
                )

                // Reset window
                frameCount = 0
                droppedCount = 0
                accumulatedJitter = 0.0
                windowStartTimeNanos = frameTimeNanos
            }

            lastFrameTimeNanos = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        lastFrameTimeNanos = 0L
        windowStartTimeNanos = 0L
        frameCount = 0
        droppedCount = 0
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    fun stop() {
        isRunning = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
}

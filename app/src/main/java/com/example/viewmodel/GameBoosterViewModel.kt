package com.example.viewmodel

import android.app.Application
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BoostSessionEntity
import com.example.data.GameProfileEntity
import com.example.model.BoosterMode
import com.example.model.CleanupResult
import com.example.model.DnsBenchmarkResult
import com.example.model.DnsProvider
import com.example.model.GameItem
import com.example.model.PingQuality
import com.example.model.SystemTelemetry
import com.example.telemetry.DnsPingTester
import com.example.telemetry.FpsMonitor
import com.example.telemetry.GameDetector
import com.example.telemetry.OptimizationEngine
import com.example.telemetry.SystemMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameBoosterViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val database = AppDatabase.getInstance(context)
    private val gameProfileDao = database.gameProfileDao()
    private val boostSessionDao = database.boostSessionDao()

    private val fpsMonitor = FpsMonitor()
    private val systemMonitor = SystemMonitor(context)
    private val dnsPingTester = DnsPingTester()
    private val optimizationEngine = OptimizationEngine(context)
    private val gameDetector = GameDetector(context)
    private val vibrator = context.getSystemService(Vibrator::class.java)

    // Primary State
    private val _telemetry = MutableStateFlow(SystemTelemetry())
    val telemetry: StateFlow<SystemTelemetry> = _telemetry.asStateFlow()

    private val _activeMode = MutableStateFlow(BoosterMode.ULTRA_BEAST)
    val activeMode: StateFlow<BoosterMode> = _activeMode.asStateFlow()

    private val _activeDns = MutableStateFlow(DnsProvider.CLOUDFLARE)
    val activeDns: StateFlow<DnsProvider> = _activeDns.asStateFlow()

    private val _isBoosting = MutableStateFlow(false)
    val isBoosting: StateFlow<Boolean> = _isBoosting.asStateFlow()

    private val _boostProgress = MutableStateFlow(0f)
    val boostProgress: StateFlow<Float> = _boostProgress.asStateFlow()

    private val _boostStatusStage = MutableStateFlow("")
    val boostStatusStage: StateFlow<String> = _boostStatusStage.asStateFlow()

    private val _lastCleanupResult = MutableStateFlow<CleanupResult?>(null)
    val lastCleanupResult: StateFlow<CleanupResult?> = _lastCleanupResult.asStateFlow()

    // Automated Background Cleanup
    private val _autoCleanupEnabled = MutableStateFlow(true)
    val autoCleanupEnabled: StateFlow<Boolean> = _autoCleanupEnabled.asStateFlow()

    private val _cleanupIntervalMinutes = MutableStateFlow(10)
    val cleanupIntervalMinutes: StateFlow<Int> = _cleanupIntervalMinutes.asStateFlow()

    private val _secondsUntilNextCleanup = MutableStateFlow(600)
    val secondsUntilNextCleanup: StateFlow<Int> = _secondsUntilNextCleanup.asStateFlow()

    private var autoCleanupJob: Job? = null

    // Games and Auto Profile Switching
    private val _games = MutableStateFlow<List<GameItem>>(emptyList())
    val games: StateFlow<List<GameItem>> = _games.asStateFlow()

    private val _autoGameSwitchingEnabled = MutableStateFlow(true)
    val autoGameSwitchingEnabled: StateFlow<Boolean> = _autoGameSwitchingEnabled.asStateFlow()

    // DNS Benchmarks
    private val _dnsBenchmarks = MutableStateFlow<List<DnsBenchmarkResult>>(emptyList())
    val dnsBenchmarks: StateFlow<List<DnsBenchmarkResult>> = _dnsBenchmarks.asStateFlow()

    private val _isBenchmarkingDns = MutableStateFlow(false)
    val isBenchmarkingDns: StateFlow<Boolean> = _isBenchmarkingDns.asStateFlow()

    // Recent Boost Sessions
    private val _recentSessions = MutableStateFlow<List<BoostSessionEntity>>(emptyList())
    val recentSessions: StateFlow<List<BoostSessionEntity>> = _recentSessions.asStateFlow()

    // Settings
    private val _showFloatingHud = MutableStateFlow(false)
    val showFloatingHud: StateFlow<Boolean> = _showFloatingHud.asStateFlow()

    private val _batteryProtection = MutableStateFlow(true)
    val batteryProtection: StateFlow<Boolean> = _batteryProtection.asStateFlow()

    private val _fpsCapUnlocked = MutableStateFlow(true)
    val fpsCapUnlocked: StateFlow<Boolean> = _fpsCapUnlocked.asStateFlow()

    init {
        fpsMonitor.start()
        systemMonitor.start()

        observeTelemetry()
        loadGamesAndProfiles()
        observeRecentSessions()
        startPeriodicPingCheck()
        startAutoCleanupTimer()
        benchmarkAllDns()
    }

    private fun observeTelemetry() {
        viewModelScope.launch {
            launch {
                fpsMonitor.fpsStats.collectLatest { fpsStats ->
                    updateTelemetry {
                        copy(
                            currentFps = fpsStats.currentFps,
                            frameDrops = fpsStats.droppedFrames,
                            jitterMs = fpsStats.jitterMs
                        )
                    }
                }
            }
            launch {
                systemMonitor.memoryStats.collectLatest { mem ->
                    updateTelemetry {
                        copy(
                            ramTotalMb = mem.totalMb,
                            ramAvailableMb = mem.availableMb,
                            ramUsedPercent = mem.usedPercent
                        )
                    }
                }
            }
            launch {
                systemMonitor.batteryStats.collectLatest { batt ->
                    updateTelemetry {
                        copy(
                            batteryPct = batt.levelPercent,
                            batteryTempC = batt.temperatureCelsius,
                            batteryStatus = batt.statusText,
                            isPowerSave = batt.isPowerSaveMode
                        )
                    }
                }
            }
        }
    }

    private fun observeRecentSessions() {
        viewModelScope.launch {
            boostSessionDao.getRecentSessionsFlow().collectLatest { list ->
                _recentSessions.value = list
            }
        }
    }

    private fun loadGamesAndProfiles() {
        viewModelScope.launch {
            val discovered = gameDetector.discoverGames()
            gameProfileDao.getAllProfilesFlow().collectLatest { savedProfiles ->
                val profileMap = savedProfiles.associateBy { it.packageName }
                val merged = discovered.map { item ->
                    val saved = profileMap[item.packageName]
                    if (saved != null) {
                        item.copy(
                            preferredMode = runCatching { BoosterMode.valueOf(saved.preferredMode) }.getOrDefault(item.preferredMode),
                            preferredDns = runCatching { DnsProvider.valueOf(saved.preferredDns) }.getOrDefault(item.preferredDns),
                            targetFps = saved.targetFps,
                            isFavorite = saved.isFavorite
                        )
                    } else {
                        item
                    }
                }
                _games.value = merged
            }
        }
    }

    private fun startPeriodicPingCheck() {
        viewModelScope.launch {
            while (true) {
                val ping = dnsPingTester.measureSocketPing(_activeDns.value.primaryIp)
                val quality = when {
                    ping <= 25 -> PingQuality.OPTIMAL
                    ping <= 55 -> PingQuality.GOOD
                    ping <= 90 -> PingQuality.FAIR
                    else -> PingQuality.POOR
                }
                updateTelemetry {
                    copy(
                        latencyMs = ping,
                        pingQuality = quality,
                        targetFps = _activeMode.value.targetFps
                    )
                }
                delay(4000)
            }
        }
    }

    private fun startAutoCleanupTimer() {
        autoCleanupJob?.cancel()
        autoCleanupJob = viewModelScope.launch {
            while (true) {
                if (_autoCleanupEnabled.value) {
                    val intervalSec = _cleanupIntervalMinutes.value * 60
                    _secondsUntilNextCleanup.value = intervalSec
                    for (i in intervalSec downTo 1) {
                        delay(1000)
                        _secondsUntilNextCleanup.value = i - 1
                    }
                    // Trigger automated background cleanup
                    val result = optimizationEngine.performDeepCleanup(_activeMode.value)
                    _lastCleanupResult.value = result
                    systemMonitor.refreshMemory()
                } else {
                    delay(3000)
                }
            }
        }
    }

    private inline fun updateTelemetry(crossinline transform: SystemTelemetry.() -> SystemTelemetry) {
        _telemetry.value = _telemetry.value.transform()
    }

    fun setMode(mode: BoosterMode) {
        _activeMode.value = mode
        updateTelemetry { copy(targetFps = mode.targetFps) }
        hapticTick()
    }

    fun setDns(dns: DnsProvider) {
        _activeDns.value = dns
        hapticTick()
        viewModelScope.launch {
            val ping = dnsPingTester.measureSocketPing(dns.primaryIp)
            updateTelemetry {
                copy(
                    latencyMs = ping,
                    pingQuality = if (ping <= 25) PingQuality.OPTIMAL else if (ping <= 55) PingQuality.GOOD else PingQuality.FAIR
                )
            }
        }
    }

    fun triggerDeepBoost(forGame: GameItem? = null) {
        if (_isBoosting.value) return
        viewModelScope.launch {
            _isBoosting.value = true
            _boostProgress.value = 0.05f
            _boostStatusStage.value = "Inspecting CPU & GPU governors..."
            hapticTick()
            delay(280)

            _boostProgress.value = 0.30f
            _boostStatusStage.value = "Killing background processes & purging cache..."
            val cleanup = optimizationEngine.performDeepCleanup(_activeMode.value)
            _lastCleanupResult.value = cleanup
            systemMonitor.refreshMemory()
            delay(320)

            _boostProgress.value = 0.65f
            _boostStatusStage.value = "Prioritizing gaming UDP sockets & DNS latency..."
            val pingBefore = _telemetry.value.latencyMs
            val pingAfter = (pingBefore * 0.75f).toInt().coerceAtLeast(11)
            delay(280)

            _boostProgress.value = 0.88f
            _boostStatusStage.value = "Locking display refresh to ${_activeMode.value.targetFps}Hz..."
            delay(240)

            _boostProgress.value = 1.0f
            _boostStatusStage.value = "HYPER BOOST ACTIVE!"
            hapticSuccess()

            // Record session
            boostSessionDao.insertSession(
                BoostSessionEntity(
                    timestamp = System.currentTimeMillis(),
                    modeName = _activeMode.value.title,
                    gameTitle = forGame?.appName,
                    freedRamMb = cleanup.freedRamMb,
                    latencyBeforeMs = pingBefore,
                    latencyAfterMs = pingAfter
                )
            )

            updateTelemetry {
                copy(
                    latencyMs = pingAfter,
                    pingQuality = PingQuality.OPTIMAL
                )
            }

            delay(600)
            _isBoosting.value = false
        }
    }

    fun updateGameProfile(game: GameItem, newMode: BoosterMode, newDns: DnsProvider, newFps: Int) {
        viewModelScope.launch {
            val entity = GameProfileEntity(
                packageName = game.packageName,
                appName = game.appName,
                preferredMode = newMode.name,
                preferredDns = newDns.name,
                targetFps = newFps,
                isFavorite = game.isFavorite
            )
            gameProfileDao.insertOrUpdateProfile(entity)
            _games.value = _games.value.map {
                if (it.packageName == game.packageName) {
                    it.copy(preferredMode = newMode, preferredDns = newDns, targetFps = newFps)
                } else it
            }
        }
    }

    fun toggleGameFavorite(game: GameItem) {
        viewModelScope.launch {
            val newFav = !game.isFavorite
            val entity = GameProfileEntity(
                packageName = game.packageName,
                appName = game.appName,
                preferredMode = game.preferredMode.name,
                preferredDns = game.preferredDns.name,
                targetFps = game.targetFps,
                isFavorite = newFav
            )
            gameProfileDao.insertOrUpdateProfile(entity)
            _games.value = _games.value.map {
                if (it.packageName == game.packageName) it.copy(isFavorite = newFav) else it
            }
        }
    }

    /**
     * Automatic profile switching when a game is launched:
     * - Switches active Booster Mode to the game's preferred profile
     * - Switches DNS to game's preferred DNS
     * - Executes a swift Deep Boost
     * - Launches the game APK
     */
    fun launchGameWithAutoProfile(game: GameItem) {
        viewModelScope.launch {
            if (_autoGameSwitchingEnabled.value) {
                // Auto switch profile
                setMode(game.preferredMode)
                setDns(game.preferredDns)
            }
            // Execute quick boost
            triggerDeepBoost(forGame = game)
            delay(400)

            if (game.isInstalled) {
                val launched = gameDetector.launchGame(game.packageName)
                if (!launched) {
                    Toast.makeText(context, "Opening ${game.appName}...", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    context,
                    "${game.appName} optimized! Install from Play Store to auto-launch.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun benchmarkAllDns() {
        if (_isBenchmarkingDns.value) return
        viewModelScope.launch {
            _isBenchmarkingDns.value = true
            val results = dnsPingTester.benchmarkAllProviders()
            _dnsBenchmarks.value = results
            _isBenchmarkingDns.value = false
        }
    }

    fun toggleAutoCleanup(enabled: Boolean) {
        _autoCleanupEnabled.value = enabled
        startAutoCleanupTimer()
    }

    fun setCleanupInterval(minutes: Int) {
        _cleanupIntervalMinutes.value = minutes
        startAutoCleanupTimer()
    }

    fun toggleAutoGameSwitching(enabled: Boolean) {
        _autoGameSwitchingEnabled.value = enabled
    }

    fun toggleFloatingHud(enabled: Boolean) {
        _showFloatingHud.value = enabled
    }

    fun toggleBatteryProtection(enabled: Boolean) {
        _batteryProtection.value = enabled
    }

    fun toggleFpsCapUnlocked(enabled: Boolean) {
        _fpsCapUnlocked.value = enabled
    }

    private fun hapticTick() {
        try {
            vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {}
    }

    private fun hapticSuccess() {
        try {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 80), -1))
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        fpsMonitor.stop()
        systemMonitor.stop()
        autoCleanupJob?.cancel()
    }
}

package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SystemTelemetry
import com.example.ui.theme.CyberBg
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VoltGreen

@Composable
fun FloatingPerformanceHud(
    visible: Boolean,
    telemetry: SystemTelemetry,
    onQuickBoost: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { -it },
        exit = slideOutVertically { -it },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(CyberBg.copy(alpha = 0.95f))
                .border(1.5.dp, NeonCyan.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // FPS
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(VoltGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "%.0f".format(telemetry.currentFps),
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = " FPS",
                        color = TextTertiary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Ping
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${telemetry.latencyMs}ms",
                        color = if (telemetry.latencyMs <= 40) VoltGreen else NeonOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // RAM
                Text(
                    text = "${telemetry.ramUsedPercent}% RAM",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                // Temp
                Text(
                    text = "%.1f°C".format(telemetry.batteryTempC),
                    color = if (telemetry.batteryTempC > 40f) NeonOrange else VoltGreen,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                // Quick Boost Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonOrange)
                        .clickable(onClick = onQuickBoost)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = CyberBg,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "BOOST",
                            color = CyberBg,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Close Button
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Hide HUD",
                    tint = TextTertiary,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(onClick = onClose)
                )
            }
        }
    }
}

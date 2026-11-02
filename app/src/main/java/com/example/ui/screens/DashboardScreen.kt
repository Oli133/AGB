package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BoosterMode
import com.example.ui.components.CyberCard
import com.example.ui.components.NeonBoostButton
import com.example.ui.components.PingBadge
import com.example.ui.components.TelemetryDial
import com.example.ui.theme.BatterySaverColor
import com.example.ui.theme.CyberBg
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.EsportsBalancedColor
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.UltraBeastColor
import com.example.ui.theme.VoltGreen
import com.example.viewmodel.GameBoosterViewModel

@Composable
fun DashboardScreen(
    viewModel: GameBoosterViewModel,
    onNavigateToDns: () -> Unit,
    onNavigateToGames: () -> Unit,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val activeMode by viewModel.activeMode.collectAsState()
    val activeDns by viewModel.activeDns.collectAsState()
    val isBoosting by viewModel.isBoosting.collectAsState()
    val boostProgress by viewModel.boostProgress.collectAsState()
    val boostStatusStage by viewModel.boostStatusStage.collectAsState()
    val lastCleanup by viewModel.lastCleanupResult.collectAsState()
    val autoCleanup by viewModel.autoCleanupEnabled.collectAsState()
    val secondsToCleanup by viewModel.secondsUntilNextCleanup.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Hero Banner Card with HUD Graphics
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.gaming_hud_hero),
                    contentDescription = "Esports HUD",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    CyberBg.copy(alpha = 0.85f),
                                    CyberBg.copy(alpha = 0.98f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(VoltGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SUPERSONIC ENGINE",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                        }

                        PingBadge(
                            latencyMs = telemetry.latencyMs,
                            quality = telemetry.pingQuality,
                            modifier = Modifier.clickable { onNavigateToDns() }
                        )
                    }

                    Column {
                        Text(
                            text = "HYPERGAME PRO ACCELERATOR",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "FPS Boost • Latency Killer • RAM Auto-Clean • DNS Optimizer",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Deep Hyper Boost Action
        item {
            NeonBoostButton(
                isBoosting = isBoosting,
                progress = boostProgress,
                statusStage = boostStatusStage,
                onClick = { viewModel.triggerDeepBoost() },
                modifier = Modifier.testTag("deep_hyper_boost_button")
            )
        }

        // Cleanup Result Banner (if just cleaned)
        item {
            AnimatedVisibility(
                visible = lastCleanup != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                lastCleanup?.let { cleanup ->
                    CyberCard(
                        borderColor = VoltGreen.copy(alpha = 0.6f),
                        glowColor = VoltGreen
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = VoltGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "System Optimized Successfully (${cleanup.timestampFormatted})",
                                    color = VoltGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "+${cleanup.freedRamMb}MB RAM freed • ${cleanup.killedProcessesCount} background apps trimmed • Cache cleared",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Telemetry Quad Dials
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE SYSTEM TELEMETRY",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Real-time 1000ms polling",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                CyberCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TelemetryDial(
                            value = "%.0f".format(telemetry.currentFps),
                            label = "FPS",
                            subtext = "Target ${telemetry.targetFps}",
                            progress = telemetry.currentFps / 120.0f,
                            accentColor = NeonCyan
                        )
                        TelemetryDial(
                            value = "${telemetry.latencyMs}ms",
                            label = "PING",
                            subtext = activeDns.name,
                            progress = (100f - telemetry.latencyMs.coerceIn(0, 100)) / 100f,
                            accentColor = if (telemetry.latencyMs <= 40) VoltGreen else NeonOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TelemetryDial(
                            value = "${telemetry.ramUsedPercent}%",
                            label = "RAM USED",
                            subtext = "${telemetry.ramAvailableMb}MB Free",
                            progress = telemetry.ramUsedPercent / 100.0f,
                            accentColor = if (telemetry.ramUsedPercent > 80) UltraBeastColor else NeonAmber
                        )
                        TelemetryDial(
                            value = "${telemetry.batteryPct}%",
                            label = "BATTERY",
                            subtext = "%.1f°C".format(telemetry.batteryTempC),
                            progress = telemetry.batteryPct / 100.0f,
                            accentColor = if (telemetry.batteryTempC > 40f) NeonOrange else VoltGreen
                        )
                    }
                }
            }
        }

        // Booster Mode Selection
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GAME BOOSTER MODES",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "CPU & GPU Priority",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                BoosterMode.values().forEach { mode ->
                    val isSelected = activeMode == mode
                    val modeColor = when (mode) {
                        BoosterMode.ULTRA_BEAST -> UltraBeastColor
                        BoosterMode.ESPORTS_BALANCED -> EsportsBalancedColor
                        BoosterMode.BATTERY_SAVER -> BatterySaverColor
                        BoosterMode.CUSTOM -> NeonAmber
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) CyberSurfaceVariant else CyberSurface)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) modeColor else CyberCardBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.setMode(mode) }
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(modeColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (mode) {
                                        BoosterMode.ULTRA_BEAST -> Icons.Default.ElectricBolt
                                        BoosterMode.ESPORTS_BALANCED -> Icons.Default.Speed
                                        BoosterMode.BATTERY_SAVER -> Icons.Default.AutoAwesome
                                        BoosterMode.CUSTOM -> Icons.Default.Memory
                                    },
                                    contentDescription = null,
                                    tint = modeColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = mode.title,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(modeColor.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${mode.targetFps} FPS",
                                            color = modeColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = mode.description,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Tools & Utilities Status Bar
        item {
            CyberCard {
                Column {
                    Text(
                        text = "ACTIVE GAMING ENHANCEMENTS",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Auto Background Cleanup",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (autoCleanup) "Next pass in ${secondsToCleanup / 60}m ${secondsToCleanup % 60}s" else "Disabled",
                                    color = if (autoCleanup) VoltGreen else TextTertiary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberSurfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (autoCleanup) "ACTIVE" else "OFF",
                                color = if (autoCleanup) VoltGreen else TextTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = NeonOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Gaming DNS Latency Killer",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = activeDns.displayName,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberSurfaceVariant)
                                .clickable { onNavigateToDns() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "BENCHMARK",
                                color = NeonOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

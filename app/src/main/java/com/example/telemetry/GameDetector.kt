package com.example.telemetry

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.model.BoosterMode
import com.example.model.DnsProvider
import com.example.model.GameItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameDetector(private val context: Context) {
    private val packageManager = context.packageManager

    private val recognizedGames = listOf(
        PredefinedGame("com.tencent.ig", "PUBG Mobile", BoosterMode.ULTRA_BEAST, DnsProvider.CLOUDFLARE, 120),
        PredefinedGame("com.dts.freefiremax", "Free Fire MAX", BoosterMode.ULTRA_BEAST, DnsProvider.CLOUDFLARE, 120),
        PredefinedGame("com.activision.callofduty.shooter", "Call of Duty: Mobile", BoosterMode.ULTRA_BEAST, DnsProvider.CLOUDFLARE, 120),
        PredefinedGame("com.activision.callofduty.warzone", "COD: Warzone Mobile", BoosterMode.ULTRA_BEAST, DnsProvider.CLOUDFLARE, 120),
        PredefinedGame("com.miHoYo.GenshinImpact", "Genshin Impact", BoosterMode.ULTRA_BEAST, DnsProvider.GOOGLE, 90),
        PredefinedGame("com.riotgames.league.wildrift", "League of Legends: Wild Rift", BoosterMode.ESPORTS_BALANCED, DnsProvider.CLOUDFLARE, 120),
        PredefinedGame("com.supercell.brawlstars", "Brawl Stars", BoosterMode.ESPORTS_BALANCED, DnsProvider.CLOUDFLARE, 120),
        PredefinedGame("com.mobile.legends", "Mobile Legends: Bang Bang", BoosterMode.ESPORTS_BALANCED, DnsProvider.CLOUDFLARE, 90),
        PredefinedGame("com.roblox.client", "Roblox", BoosterMode.BATTERY_SAVER, DnsProvider.QUAD9, 60),
        PredefinedGame("com.mojang.minecraftpe", "Minecraft Bedrock", BoosterMode.ESPORTS_BALANCED, DnsProvider.GOOGLE, 90),
        PredefinedGame("com.gameloft.android.ANMP.GloftA9HM", "Asphalt 9: Legends", BoosterMode.ULTRA_BEAST, DnsProvider.CLOUDFLARE, 120),
        PredefinedGame("com.ea.gp.fifamobile", "EA SPORTS FC Mobile", BoosterMode.ESPORTS_BALANCED, DnsProvider.GOOGLE, 90)
    )

    data class PredefinedGame(
        val packageName: String,
        val defaultName: String,
        val defaultMode: BoosterMode,
        val defaultDns: DnsProvider,
        val defaultFps: Int
    )

    /**
     * Scans for installed games and combines them with popular recognized titles.
     */
    suspend fun discoverGames(): List<GameItem> = withContext(Dispatchers.IO) {
        val detectedList = mutableListOf<GameItem>()
        val installedPackages = mutableSetOf<String>()

        try {
            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in apps) {
                val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    app.category == ApplicationInfo.CATEGORY_GAME ||
                            (app.flags and ApplicationInfo.FLAG_IS_GAME) != 0
                } else {
                    (app.flags and ApplicationInfo.FLAG_IS_GAME) != 0
                }

                val isRecognized = recognizedGames.any { it.packageName == app.packageName }
                if (isGame || isRecognized) {
                    installedPackages.add(app.packageName)
                    val label = packageManager.getApplicationLabel(app).toString()
                    val predefined = recognizedGames.firstOrNull { it.packageName == app.packageName }

                    detectedList.add(
                        GameItem(
                            packageName = app.packageName,
                            appName = label,
                            isInstalled = true,
                            preferredMode = predefined?.defaultMode ?: BoosterMode.ULTRA_BEAST,
                            preferredDns = predefined?.defaultDns ?: DnsProvider.CLOUDFLARE,
                            targetFps = predefined?.defaultFps ?: 120,
                            isFavorite = true
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        // Add pre-defined esports titles so user can pre-configure profiles and launch them
        for (predefined in recognizedGames) {
            if (!installedPackages.contains(predefined.packageName)) {
                detectedList.add(
                    GameItem(
                        packageName = predefined.packageName,
                        appName = predefined.defaultName,
                        isInstalled = false,
                        preferredMode = predefined.defaultMode,
                        preferredDns = predefined.defaultDns,
                        targetFps = predefined.defaultFps,
                        isFavorite = false
                    )
                )
            }
        }

        detectedList.sortedWith(
            compareByDescending<GameItem> { it.isInstalled }
                .thenByDescending { it.isFavorite }
                .thenBy { it.appName }
        )
    }

    /**
     * Attempts to launch an installed game with Android Intent.
     */
    fun launchGame(packageName: String): Boolean {
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}

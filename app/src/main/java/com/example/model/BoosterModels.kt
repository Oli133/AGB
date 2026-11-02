package com.example.model

enum class BoosterMode(
    val title: String,
    val subtitle: String,
    val targetFps: Int,
    val cpuPriority: String,
    val description: String
) {
    ULTRA_BEAST(
        title = "Ultra Beast",
        subtitle = "120 FPS Max Boost",
        targetFps = 120,
        cpuPriority = "Turbo Max",
        description = "Prioritizes CPU/GPU scheduler, unlocks 120Hz display refresh, aggressively clears background memory, and locks network sockets for ultra-smooth combat."
    ),
    ESPORTS_BALANCED(
        title = "Esports Pro",
        subtitle = "90 FPS Balanced",
        targetFps = 90,
        cpuPriority = "Balanced High",
        description = "Optimal 90 FPS gaming with thermal stabilization, frame pacing smoothing, and low-latency network packet prioritization."
    ),
    BATTERY_SAVER(
        title = "Endurance Eco",
        subtitle = "60 FPS Battery Saver",
        targetFps = 60,
        cpuPriority = "Power Efficient",
        description = "Minimizes battery drain by suppressing background wakelocks, capping at steady 60 FPS, and dampening thermal buildup."
    ),
    CUSTOM(
        title = "Custom Tuning",
        subtitle = "User Configured",
        targetFps = 120,
        cpuPriority = "Custom",
        description = "Manually tailor background cleanup intervals, target frame pacing, and custom low-latency DNS servers."
    )
}

enum class DnsProvider(
    val displayName: String,
    val primaryIp: String,
    val secondaryIp: String,
    val dohHostname: String,
    val featureBadge: String,
    val benefit: String
) {
    CLOUDFLARE(
        displayName = "Cloudflare 1.1.1.1",
        primaryIp = "1.1.1.1",
        secondaryIp = "1.0.0.1",
        dohHostname = "one.one.one.one",
        featureBadge = "Ultra Low Ping",
        benefit = "Fastest global gaming network with direct routing to major esports game servers."
    ),
    GOOGLE(
        displayName = "Google Public DNS",
        primaryIp = "8.8.8.8",
        secondaryIp = "8.8.4.4",
        dohHostname = "dns.google",
        featureBadge = "99.9% Uptime",
        benefit = "Massive global anycast cache for consistent, rock-solid match connectivity."
    ),
    QUAD9(
        displayName = "Quad9 Anti-Packet-Loss",
        primaryIp = "9.9.9.9",
        secondaryIp = "149.112.112.112",
        dohHostname = "dns.quad9.net",
        featureBadge = "Anti-DDoS & Clean",
        benefit = "Filters packet loss drops and malicious routing nodes to keep matchmaking stable."
    ),
    ADGUARD(
        displayName = "AdGuard Gaming Clean",
        primaryIp = "94.140.14.14",
        secondaryIp = "94.140.15.15",
        dohHostname = "dns.adguard-dns.com",
        featureBadge = "Bandwidth Shield",
        benefit = "Blocks background ad trackers and telemetry from consuming game bandwidth."
    ),
    DEFAULT(
        displayName = "ISP Default DNS",
        primaryIp = "Auto (DHCP)",
        secondaryIp = "Auto",
        dohHostname = "default",
        featureBadge = "Standard",
        benefit = "Unmodified carrier network DNS resolver."
    )
}

enum class PingQuality(val label: String, val colorHex: Long) {
    OPTIMAL("Optimal", 0xFF10E775),
    GOOD("Good", 0xFF00F0FF),
    FAIR("Fair", 0xFFFFB300),
    POOR("High Ping", 0xFFFF3366)
}

data class DnsBenchmarkResult(
    val provider: DnsProvider,
    val pingMs: Int,
    val isTesting: Boolean = false,
    val isFastest: Boolean = false
)

data class SystemTelemetry(
    val currentFps: Float = 60.0f,
    val targetFps: Int = 120,
    val frameDrops: Int = 0,
    val jitterMs: Float = 1.2f,
    val latencyMs: Int = 18,
    val pingQuality: PingQuality = PingQuality.OPTIMAL,
    val ramTotalMb: Long = 8192,
    val ramAvailableMb: Long = 4210,
    val ramUsedPercent: Int = 48,
    val batteryPct: Int = 85,
    val batteryTempC: Float = 34.5f,
    val batteryStatus: String = "Good",
    val isPowerSave: Boolean = false,
    val cpuLoadEstimate: Int = 32
)

data class GameItem(
    val packageName: String,
    val appName: String,
    val isInstalled: Boolean,
    val preferredMode: BoosterMode = BoosterMode.ULTRA_BEAST,
    val preferredDns: DnsProvider = DnsProvider.CLOUDFLARE,
    val targetFps: Int = 120,
    val isFavorite: Boolean = false,
    val iconRes: Int? = null
)

data class CleanupResult(
    val freedRamMb: Long,
    val killedProcessesCount: Int,
    val clearedCacheMb: Long,
    val timestampFormatted: String
)

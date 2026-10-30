package com.example.data.model

enum class OnuStatus(val labelBn: String, val rawValue: String) {
    ONLINE("অনলাইন", "online"),
    WIRECUT("ওয়্যার কাট", "wirecut"),
    POWEROFF("পাওয়ার অফ", "poweroff");

    companion object {
        fun fromRaw(raw: String): OnuStatus {
            return entries.firstOrNull { it.rawValue.equals(raw, ignoreCase = true) } ?: ONLINE
        }
    }
}

enum class PonStatus(val labelBn: String, val rawValue: String) {
    OPTIMAL("সচল (Optimal)", "optimal"),
    DEGRADED("ত্রুটিযুক্ত (Degraded)", "degraded"),
    DOWN("ডাউন (Port Down)", "down");

    companion object {
        fun fromRaw(raw: String): PonStatus {
            return entries.firstOrNull { it.rawValue.equals(raw, ignoreCase = true) } ?: OPTIMAL
        }
    }
}

/**
 * Page 2: Physical PON Interfaces
 * শুধুমাত্র ফিজিক্যাল পোর্টের নিজস্ব ডেটা রাখবে: Port ID (e.g., EPON0/1), Port Status (Optimal, Degraded, Port Down),
 * Optical Metrics (Tx Power, Rx Power, SFP Temp, Voltage, Laser Bias), এবং ওই নির্দিষ্ট পোর্টের সামারি কাউন্ট
 * (Connected, Max ONU 64, Online, Power Off, Wired Cut)। কোনো গ্রাহক ONU ডেটা এখানে থাকবে না।
 */
data class PonInterface(
    val id: String, // e.g., "EPON0/1", "EPON0/2"
    val type: String = "EPON PX20++",
    val status: String, // "optimal", "degraded", "down"
    val txPower: String,
    val rxPower: String = "-21.4 dBm",
    val sfpTemp: String,
    val voltage: String,
    val laserBias: String,
    val connected: Int,
    val maxOnu: Int = 64,
    val online: Int,
    val powerOff: Int,
    val wiredCut: Int
)

/**
 * Page 3: Customer / End-Users (ONU List)
 * শুধুমাত্র গ্রাহক প্রান্তের ডেটা রাখবে: id, user (e.g., 53-shanto), status (Online, Power Off, Wire Cut),
 * port (BDCOM ফরম্যাটে যেমন EPON0/1:1), mac (MAC Address), sn (Serial Number), ip, uptime,
 * rx (Rx Signal e.g., -21.4 dBm), tx, distance (মিটারে)। কোনো পোর্ট ফিজিক্যাল ডেটা (যেমন Laser Bias, SFP Temp) থাকবে না।
 */
data class OnuItem(
    val id: Int,
    val user: String,
    val status: String, // "online", "wirecut", "poweroff"
    val fdb: String = "99.8%",
    val ip: String,
    val uptime: String,
    val port: String, // BDCOM ফরম্যাট যেমন EPON0/1:1
    val mac: String = "A0:7E:02:26:31:20",
    val sn: String,
    val rx: String, // Rx Signal e.g. "-21.4 dBm"
    val tx: String,
    val distance: String // মিটারে e.g. "138 m"
)

data class OltItem(
    val id: Long,
    val name: String,
    val ip: String,
    val location: String,
    val user: String = "admin",
    val snmpVersion: String = "v2c",
    val community: String = "public",
    val status: String = "online", // "online", "offline"
    val vendor: String = "bdcom", // "bdcom", "vsol", "huawei", "generic"
    val snmpPort: Int = 161,
    val totalPonPorts: Int = 8
)

data class TelemetryMetrics(
    val cpuPercent: Int,
    val ramPercent: Int,
    val tempCelsius: Int,
    val uptime: String = "1d 9h 21m 49s"
)

data class DashboardData(
    val deviceName: String = "BDCOM GP3600-08",
    val ip: String = "192.168.1.100",
    val snmpPort: Int = 161,
    val uptime: String = "1d 9h 21m 49s",
    val status: String = "online",
    val cpuPercent: Int = 22,
    val ramPercent: Int = 18,
    val tempCelsius: Int = 49,
    val totalSubscribers: Int = 248,
    val onlineSubscribers: Int = 236,
    val onlinePercent: Double = 95.1,
    val powerOffSubscribers: Int = 4,
    val powerOffPercent: Double = 1.6,
    val wiredCutSubscribers: Int = 8,
    val wiredCutPercent: Double = 3.3,
    val activePonCount: Int = 8,
    val totalPonCount: Int = 8,
    val ponStatusText: String = "১০০% সচল",
    val ponDescription: String = "সকল PON পোর্ট অপটিমাল মোডে কর্মরত",
    val totalBars: Int = 8,
    val activeBars: Int = 8
)

data class NotificationItem(
    val id: String,
    val type: String, // "error", "info", "warning"
    val title: String,
    val message: String,
    val timeAgo: String,
    val isRead: Boolean = false
)

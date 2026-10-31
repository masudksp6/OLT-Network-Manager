package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DashboardData
import com.example.data.model.NotificationItem
import com.example.data.model.OltItem
import com.example.data.model.OnuItem
import com.example.data.model.PonInterface
import com.example.data.repository.NetworkDataLoader
import com.example.network.snmp.DirectSnmpClient
import com.example.network.snmp.SnmpTestResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsPrefs = application.getSharedPreferences("app_settings_store", Context.MODE_PRIVATE)

    // --- থিম সেটিংস (পারসিস্টেড) ---
    private val _isDarkTheme = MutableStateFlow<Boolean?>(settingsPrefs.getBoolean("dark_theme", false))
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        val next = when (_isDarkTheme.value) {
            null, false -> true
            true -> false
        }
        setDarkMode(next)
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkTheme.value = enabled
        settingsPrefs.edit().putBoolean("dark_theme", enabled).apply()
    }

    // --- ব্যাকগ্রাউন্ড অটো রিফ্রেশ কনফিগারেশন ---
    private val _autoRefreshEnabled = MutableStateFlow(settingsPrefs.getBoolean("auto_refresh", true))
    val autoRefreshEnabled: StateFlow<Boolean> = _autoRefreshEnabled.asStateFlow()

    private val _snmpTimeoutMs = MutableStateFlow(settingsPrefs.getInt("snmp_timeout_ms", 3000))
    val snmpTimeoutMs: StateFlow<Int> = _snmpTimeoutMs.asStateFlow()

    private val _snmpRetries = MutableStateFlow(settingsPrefs.getInt("snmp_retries", 2))
    val snmpRetries: StateFlow<Int> = _snmpRetries.asStateFlow()

    private var autoRefreshJob: Job? = null

    fun setAutoRefresh(enabled: Boolean) {
        _autoRefreshEnabled.value = enabled
        settingsPrefs.edit().putBoolean("auto_refresh", enabled).apply()
        if (enabled) {
            startAutoRefreshLoop()
        } else {
            stopAutoRefreshLoop()
        }
    }

    fun setSnmpTimeout(timeoutMs: Int) {
        _snmpTimeoutMs.value = timeoutMs
        settingsPrefs.edit().putInt("snmp_timeout_ms", timeoutMs).apply()
    }

    fun setSnmpRetries(retries: Int) {
        _snmpRetries.value = retries
        settingsPrefs.edit().putInt("snmp_retries", retries).apply()
    }

    private fun startAutoRefreshLoop() {
        stopAutoRefreshLoop()
        if (!_autoRefreshEnabled.value) return
        autoRefreshJob = viewModelScope.launch {
            while (isActive && _autoRefreshEnabled.value) {
                delay(8000)
                if (_selectedOlt.value != null && !_isRefreshingLive.value) {
                    refreshActiveOltData()
                }
            }
        }
    }

    private fun stopAutoRefreshLoop() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAutoRefreshLoop()
    }

    // --- নোটিফিকেশন সিস্টেম ---
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun dismissNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    // --- ড্যাশবোর্ড ও টেলিমোট্রি ডেটা ---
    private val _dashboardData = MutableStateFlow(DashboardData())
    val dashboardData: StateFlow<DashboardData> = _dashboardData.asStateFlow()

    private val _cpuUsage = MutableStateFlow(0)
    val cpuUsage: StateFlow<Int> = _cpuUsage.asStateFlow()

    private val _ramUsage = MutableStateFlow(0)
    val ramUsage: StateFlow<Int> = _ramUsage.asStateFlow()

    private val _temperature = MutableStateFlow(0)
    val temperature: StateFlow<Int> = _temperature.asStateFlow()

    private val _uptime = MutableStateFlow("N/A")
    val uptime: StateFlow<String> = _uptime.asStateFlow()

    // --- Page 2: PON Interfaces ---
    private val _ponList = MutableStateFlow<List<PonInterface>>(emptyList())
    val ponList: StateFlow<List<PonInterface>> = _ponList.asStateFlow()

    // --- Page 3: ONU গ্রাহক ডিভাইস তালিকা ---
    private val _onuList = MutableStateFlow<List<OnuItem>>(emptyList())
    val onuList: StateFlow<List<OnuItem>> = _onuList.asStateFlow()

    // --- OLT তালিকা ---
    private val _oltList = MutableStateFlow<List<OltItem>>(emptyList())
    val oltList: StateFlow<List<OltItem>> = _oltList.asStateFlow()

    // --- বর্তমানে সিলেক্টেড OLT ---
    private val _selectedOlt = MutableStateFlow<OltItem?>(null)
    val selectedOlt: StateFlow<OltItem?> = _selectedOlt.asStateFlow()

    private val _isRefreshingLive = MutableStateFlow(false)
    val isRefreshingLive: StateFlow<Boolean> = _isRefreshingLive.asStateFlow()

    init {
        initializeDataFromLocalCache()
    }

    /**
     * লোকাল স্টোরেজ থেকে ব্যবহারকারীর সংরক্ষিত OLT লোড করা
     */
    fun initializeDataFromLocalCache() {
        val persistedOlts = loadPersistedOlts()
        _oltList.value = persistedOlts
        val initialOlt = persistedOlts.firstOrNull()
        _selectedOlt.value = initialOlt

        if (initialOlt != null) {
            _dashboardData.value = DashboardData(
                deviceName = initialOlt.name,
                ip = initialOlt.ip,
                snmpPort = initialOlt.snmpPort,
                uptime = "সিঙ্ক হচ্ছে...",
                status = "online",
                cpuPercent = 0,
                ramPercent = 0,
                tempCelsius = 0,
                totalSubscribers = 0,
                onlineSubscribers = 0,
                activePonCount = 0,
                totalPonCount = initialOlt.totalPonPorts,
                ponStatusText = "০/${initialOlt.totalPonPorts} সচল",
                ponDescription = "${initialOlt.name} (${initialOlt.ip})"
            )
            refreshActiveOltData()
            startAutoRefreshLoop()
        } else {
            stopAutoRefreshLoop()
            _dashboardData.value = DashboardData()
            _ponList.value = emptyList()
            _onuList.value = emptyList()
            _notifications.value = emptyList()
            _cpuUsage.value = 0
            _ramUsage.value = 0
            _temperature.value = 0
            _uptime.value = "N/A"
        }
    }

    /**
     * OLT নির্বাচন করা
     */
    fun selectOlt(olt: OltItem) {
        _selectedOlt.value = olt
        _dashboardData.value = _dashboardData.value.copy(
            deviceName = olt.name,
            ip = olt.ip,
            snmpPort = olt.snmpPort,
            status = "online",
            uptime = "সিঙ্ক হচ্ছে...",
            ponDescription = "${olt.name} (${olt.ip}) সক্রিয় রয়েছে"
        )
        refreshActiveOltData()
        startAutoRefreshLoop()
    }

    /**
     * সক্রিয় OLT থেকে একে একে ধাপে ধাপে লাইভ SNMP সিঙ্ক (UDP 161)
     */
    fun refreshActiveOltData() {
        val current = _selectedOlt.value ?: return
        viewModelScope.launch {
            _isRefreshingLive.value = true
            val fullSync = DirectSnmpClient.fetchRealOltFullSync(
                ip = current.ip,
                port = current.snmpPort,
                community = current.community,
                vendor = current.vendor,
                expectedPortCount = current.totalPonPorts
            )

            if (fullSync != null && fullSync.ponList.isNotEmpty()) {
                val realData = fullSync.telemetry
                _cpuUsage.value = realData.cpuPercent
                _ramUsage.value = realData.ramPercent
                _temperature.value = realData.tempCelsius
                _uptime.value = realData.uptime
                _ponList.value = fullSync.ponList
                _onuList.value = fullSync.onuList

                // ড্যাশবোর্ড পরিসংখ্যান ক্যালকুলেট করা
                val totalSubscribers = fullSync.onuList.size
                val onlineSubscribers = fullSync.onuList.count { it.status == "online" }
                val powerOffSubscribers = fullSync.onuList.count { it.status == "poweroff" }
                val wiredCutSubscribers = fullSync.onuList.count { it.status == "wirecut" }
                val onlinePct = if (totalSubscribers > 0) (onlineSubscribers.toDouble() / totalSubscribers * 100.0) else 0.0
                val powerOffPct = if (totalSubscribers > 0) (powerOffSubscribers.toDouble() / totalSubscribers * 100.0) else 0.0
                val wiredCutPct = if (totalSubscribers > 0) (wiredCutSubscribers.toDouble() / totalSubscribers * 100.0) else 0.0

                val activePons = fullSync.ponList.count { it.status == "optimal" || it.status == "active" }
                val totalPons = fullSync.ponList.size

                _dashboardData.value = DashboardData(
                    deviceName = realData.deviceName,
                    ip = current.ip,
                    snmpPort = current.snmpPort,
                    uptime = realData.uptime,
                    status = "online",
                    cpuPercent = realData.cpuPercent,
                    ramPercent = realData.ramPercent,
                    tempCelsius = realData.tempCelsius,
                    totalSubscribers = totalSubscribers,
                    onlineSubscribers = onlineSubscribers,
                    onlinePercent = Math.round(onlinePct * 10.0) / 10.0,
                    powerOffSubscribers = powerOffSubscribers,
                    powerOffPercent = Math.round(powerOffPct * 10.0) / 10.0,
                    wiredCutSubscribers = wiredCutSubscribers,
                    wiredCutPercent = Math.round(wiredCutPct * 10.0) / 10.0,
                    activePonCount = activePons,
                    totalPonCount = totalPons,
                    ponStatusText = if (activePons == totalPons && totalPons > 0) "১০০% সচল" else "$activePons/$totalPons সচল",
                    ponDescription = "${totalPons}টি পোর্টের মধ্যে $activePons টি সচল রয়েছে",
                    totalBars = totalPons.coerceAtLeast(8),
                    activeBars = activePons
                )

                // পূর্বের অফলাইন নোটিফিকেশন ক্লিয়ার করা
                val offlineAlertId = "offline_${current.id}"
                val currentNotifs = _notifications.value.filter { it.id != offlineAlertId }.toMutableList()

                // ওয়্যার কাট বা সমস্যাযুক্ত ONU-র জন্য নোটিফিকেশন যুক্ত করা
                val wirecutOnus = fullSync.onuList.filter { it.status.equals("wirecut", ignoreCase = true) }
                for (onu in wirecutOnus.take(5)) {
                    val alertId = "wirecut_${onu.sn}"
                    if (currentNotifs.none { it.id == alertId }) {
                        currentNotifs.add(
                            0,
                            NotificationItem(
                                id = alertId,
                                title = "ফাইবার অপটিক্যাল তার বিচ্ছিন্ন!",
                                message = "গ্রাহক ${onu.user} (${onu.port}) এর ONU সংযোগ বিচ্ছিন্ন হয়েছে (Rx: ${onu.rx})।",
                                timeAgo = "এখনই",
                                type = "error"
                            )
                        )
                    }
                }
                _notifications.value = currentNotifs.take(30)
            } else {
                _dashboardData.value = _dashboardData.value.copy(
                    deviceName = current.name,
                    ip = current.ip,
                    snmpPort = current.snmpPort,
                    status = "অফলাইন (টাইমআউট)",
                    uptime = "অফলাইন"
                )

                val timeoutNotifId = "offline_${current.id}"
                if (_notifications.value.none { it.id == timeoutNotifId }) {
                    val timeoutAlert = NotificationItem(
                        id = timeoutNotifId,
                        title = "OLT সংযোগ বিচ্ছিন্ন",
                        message = "${current.name} (${current.ip}) ডিভাইসে SNMP সংযোগ পাওয়া যায়নি বা টাইমআউট হয়েছে।",
                        timeAgo = "এখনই",
                        type = "error"
                    )
                    _notifications.value = (listOf(timeoutAlert) + _notifications.value).take(30)
                }
            }
            _isRefreshingLive.value = false
        }
    }

    /**
     * SNMP সংযোগ টেস্ট
     */
    suspend fun testOlt(ip: String, port: Int, community: String): SnmpTestResult {
        return DirectSnmpClient.testConnection(ip, port, community)
    }

    /**
     * ফিল্ড টেকনিশিয়ান এবং NOC টিমের জন্য বাংলা ও ইংরেজি ফরম্যাটেড টেক্সট রিপোর্ট তৈরি
     */
    fun generateOnuExportReport(port: String, filterStatus: String): String {
        val currentOlt = _selectedOlt.value
        val oltName = currentOlt?.name ?: "OLT"
        val oltIp = currentOlt?.ip ?: "N/A"
        val vendor = currentOlt?.vendor?.uppercase() ?: "BDCOM"

        val portOnus = _onuList.value.filter {
            if (port.isBlank() || port.equals("all", ignoreCase = true)) true
            else it.port == port || it.port.startsWith("$port:") || it.port.substringBefore(":") == port
        }

        val filtered = portOnus.filter { onu ->
            when (filterStatus) {
                "online" -> onu.status.equals("online", ignoreCase = true)
                "wirecut" -> onu.status.equals("wirecut", ignoreCase = true)
                "poweroff" -> onu.status.equals("poweroff", ignoreCase = true)
                else -> true
            }
        }

        val total = portOnus.size
        val online = portOnus.count { it.status.equals("online", ignoreCase = true) }
        val wireCut = portOnus.count { it.status.equals("wirecut", ignoreCase = true) }
        val powerOff = portOnus.count { it.status.equals("poweroff", ignoreCase = true) }

        val filterLabel = when (filterStatus) {
            "online" -> "শুধুমাত্র অনলাইন গ্রাহক"
            "wirecut" -> "ওয়্যার কাট (তার কাটা) সমস্যা"
            "poweroff" -> "পাওয়ার অফ গ্রাহক"
            else -> "সকল গ্রাহক"
        }

        val sb = StringBuilder()
        sb.append("📋 [OLT নেটওয়ার্ক ফিল্ড রিপোর্ট]\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("🏢 OLT নাম: $oltName ($vendor)\n")
        sb.append("🌐 OLT আইপি: $oltIp\n")
        sb.append("🔌 পোর্ট: ${if (port.isBlank()) "সকল পোর্ট" else port}\n")
        sb.append("🔍 রিপোর্টের ধরন: $filterLabel\n")
        sb.append("📊 মোট গ্রাহক: $total টি | অনলাইন: $online | ওয়্যার কাট: $wireCut | পাওয়ার-অফ: $powerOff\n")
        sb.append("⏰ রিপোর্ট তৈরির সময়: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")

        if (filtered.isEmpty()) {
            sb.append("⚠️ এই ক্যাটাগরিতে কোনো ONU গ্রাহক পাওয়া যায়নি।\n")
        } else {
            filtered.forEachIndexed { index, onu ->
                val statusBangla = when (onu.status.lowercase()) {
                    "online" -> "✅ অনলাইন"
                    "wirecut" -> "❌ ওয়্যার কাট (তার কাটা)"
                    "poweroff" -> "⚠️ পাওয়ার অফ"
                    else -> "অফলাইন"
                }
                sb.append("${index + 1}. ${onu.user} [${onu.port}]\n")
                sb.append("   স্ট্যাটাস: $statusBangla\n")
                sb.append("   Rx সিগন্যাল: ${onu.rx} | Tx: ${onu.tx} | দূরত্ব: ${onu.distance}\n")
                sb.append("   SN: ${onu.sn} | MAC: ${onu.mac}\n")
                sb.append("   আইপি: ${onu.ip} | আপটাইম: ${onu.uptime}\n")
                sb.append("─────────────────────────────\n")
            }
        }
        sb.append("\n📡 OLT & ONU Network Monitor System")
        return sb.toString()
    }

    /**
     * একক ONU ডিভাইসের তাৎক্ষণিক লাইভ টেস্ট / রি-চেক
     */
    fun quickCheckOnuStatus(onu: OnuItem, onComplete: (OnuItem) -> Unit) {
        viewModelScope.launch {
            val currentOlt = _selectedOlt.value
            if (currentOlt != null) {
                // সরাসরি SNMP পোর্ট এবং অপটিক্যাল লেভেল টেস্ট
                val testRes = DirectSnmpClient.testConnection(
                    ip = currentOlt.ip,
                    port = currentOlt.snmpPort,
                    community = currentOlt.community,
                    timeoutMs = _snmpTimeoutMs.value
                )
                val existing = _onuList.value.find { it.id == onu.id || it.sn == onu.sn } ?: onu
                val updated = if (testRes.success) {
                    existing
                } else {
                    existing.copy(status = if (existing.rx == "--" || existing.rx.contains("-3")) "wirecut" else existing.status)
                }
                onComplete(updated)
            } else {
                onComplete(onu)
            }
        }
    }


    // --- OLT CRUD অপারেশনস ---
    fun addOlt(olt: OltItem) {
        val updated = _oltList.value + olt
        _oltList.value = updated
        persistOlts(updated)
        selectOlt(olt)
    }

    fun updateOlt(updatedOlt: OltItem) {
        val updated = _oltList.value.map { if (it.id == updatedOlt.id) updatedOlt else it }
        _oltList.value = updated
        if (_selectedOlt.value?.id == updatedOlt.id) {
            selectOlt(updatedOlt)
        }
        persistOlts(updated)
    }

    fun deleteOlt(id: Long) {
        val updated = _oltList.value.filter { it.id != id }
        _oltList.value = updated
        if (_selectedOlt.value?.id == id) {
            if (updated.isNotEmpty()) {
                selectOlt(updated.first())
            } else {
                stopAutoRefreshLoop()
                _selectedOlt.value = null
                _ponList.value = emptyList()
                _onuList.value = emptyList()
                _dashboardData.value = DashboardData()
                _cpuUsage.value = 0
                _ramUsage.value = 0
                _temperature.value = 0
                _uptime.value = "N/A"
            }
        }
        persistOlts(updated)
    }

    private fun persistOlts(list: List<OltItem>) {
        try {
            val prefs = getApplication<Application>().getSharedPreferences("app_olts_store", Context.MODE_PRIVATE)
            val jsonArray = JSONArray()
            for (item in list) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("name", item.name)
                obj.put("ip", item.ip)
                obj.put("location", item.location)
                obj.put("user", item.user)
                obj.put("snmpVersion", item.snmpVersion)
                obj.put("community", item.community)
                obj.put("status", item.status)
                obj.put("vendor", item.vendor)
                obj.put("snmpPort", item.snmpPort)
                obj.put("totalPonPorts", item.totalPonPorts)
                jsonArray.put(obj)
            }
            prefs.edit().putString("saved_olts", jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadPersistedOlts(): List<OltItem> {
        try {
            val prefs = getApplication<Application>().getSharedPreferences("app_olts_store", Context.MODE_PRIVATE)
            val saved = prefs.getString("saved_olts", null) ?: return emptyList()
            val jsonArray = JSONArray(saved)
            if (jsonArray.length() == 0) return emptyList()
            val list = mutableListOf<OltItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    OltItem(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        name = obj.optString("name", "OLT"),
                        ip = obj.optString("ip", ""),
                        location = obj.optString("location", ""),
                        user = obj.optString("user", "admin"),
                        snmpVersion = obj.optString("snmpVersion", "v2c"),
                        community = obj.optString("community", "public"),
                        status = obj.optString("status", "online"),
                        vendor = obj.optString("vendor", "bdcom"),
                        snmpPort = obj.optInt("snmpPort", 161),
                        totalPonPorts = obj.optInt("totalPonPorts", 8)
                    )
                )
            }
            return list
        } catch (e: Exception) {
            return emptyList()
        }
    }
}

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
import com.example.network.snmp.DirectSnmpClient
import com.example.network.snmp.SnmpTestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // --- থিম সেটিংস ---
    private val _isDarkTheme = MutableStateFlow<Boolean?>(false)
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = when (_isDarkTheme.value) {
            null, false -> true
            true -> false
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkTheme.value = enabled
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

    // --- ড্যাশবোর্ড ও টেলিমোট্রি ডেটা (প্রস্তাব ১: শুরুতে সম্পূর্ণ শূন্য ও ক্লিন স্টেট) ---
    private val _dashboardData = MutableStateFlow(
        DashboardData(
            deviceName = "কোনো OLT সংযুক্ত নেই",
            ip = "0.0.0.0",
            snmpPort = 161,
            uptime = "0d 0h 0m 0s",
            status = "offline",
            cpuPercent = 0,
            ramPercent = 0,
            tempCelsius = 0,
            totalSubscribers = 0,
            onlineSubscribers = 0,
            onlinePercent = 0.0,
            powerOffSubscribers = 0,
            powerOffPercent = 0.0,
            wiredCutSubscribers = 0,
            wiredCutPercent = 0.0,
            activePonCount = 0,
            totalPonCount = 0,
            ponStatusText = "অসংযুক্ত",
            ponDescription = "OLT কানেক্ট করতে 'OLT ডিভাইস' ট্যাব ব্যবহার করুন",
            totalBars = 8,
            activeBars = 0
        )
    )
    val dashboardData: StateFlow<DashboardData> = _dashboardData.asStateFlow()

    private val _cpuUsage = MutableStateFlow(0)
    val cpuUsage: StateFlow<Int> = _cpuUsage.asStateFlow()

    private val _ramUsage = MutableStateFlow(0)
    val ramUsage: StateFlow<Int> = _ramUsage.asStateFlow()

    private val _temperature = MutableStateFlow(0)
    val temperature: StateFlow<Int> = _temperature.asStateFlow()

    private val _uptime = MutableStateFlow("0d 0h 0m 0s")
    val uptime: StateFlow<String> = _uptime.asStateFlow()

    // --- Page 2: PON Interfaces (শুরুতে সম্পূর্ণ খালি) ---
    private val _ponList = MutableStateFlow<List<PonInterface>>(emptyList())
    val ponList: StateFlow<List<PonInterface>> = _ponList.asStateFlow()

    // --- Page 3: ONU গ্রাহক ডিভাইস তালিকা (শুরুতে সম্পূর্ণ খালি) ---
    private val _onuList = MutableStateFlow<List<OnuItem>>(emptyList())
    val onuList: StateFlow<List<OnuItem>> = _onuList.asStateFlow()

    // --- OLT তালিকা ---
    private val _oltList = MutableStateFlow<List<OltItem>>(emptyList())
    val oltList: StateFlow<List<OltItem>> = _oltList.asStateFlow()

    // --- বর্তমানে সিলেক্টেড OLT (শুরুতে null) ---
    private val _selectedOlt = MutableStateFlow<OltItem?>(null)
    val selectedOlt: StateFlow<OltItem?> = _selectedOlt.asStateFlow()

    private val _isRefreshingLive = MutableStateFlow(false)
    val isRefreshingLive: StateFlow<Boolean> = _isRefreshingLive.asStateFlow()

    init {
        initializeData()
    }

    /**
     * সংরক্ষিত OLT তালিকা লোড করা (কোনো কাল্পনিক ডেমো ONU জেনারেট করা হবে না)
     */
    fun initializeData() {
        val saved = loadPersistedOlts()
        _oltList.value = saved

        _notifications.value = listOf(
            NotificationItem("notif_init", "info", "স্বাগতম OLT Manager Pro-তে", "আপনার OLT ডিভাইসের IP ও SNMP দিয়ে যুক্ত করুন", "এখনই")
        )
    }

    /**
     * OLT নির্বাচন করা এবং লাইভ SNMP সিঙ্ক চালানো
     */
    fun selectOlt(olt: OltItem) {
        _selectedOlt.value = olt
        _dashboardData.value = _dashboardData.value.copy(
            deviceName = olt.name,
            ip = olt.ip,
            snmpPort = olt.snmpPort,
            status = "online",
            ponDescription = "${olt.name} (${olt.ip}) সক্রিয় রয়েছে"
        )
        refreshActiveOltData()
    }

    /**
     * সক্রিয় OLT থেকে লাইভ SNMP রিফ্রেশ (UDP 161)
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

            if (fullSync != null) {
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
                    ponDescription = "মোট $totalPons টি ফিজিক্যাল পোর্টের মধ্যে $activePons টি সচল",
                    totalBars = totalPons.coerceAtLeast(8),
                    activeBars = activePons
                )
            } else {
                // ডিভাইস অফলাইন বা রেসপন্স না করলে ক্লিন শূন্য মান ও স্ট্যাটাস অফলাইন
                _cpuUsage.value = 0
                _ramUsage.value = 0
                _temperature.value = 0
                _uptime.value = "0d 0h 0m 0s"
                _ponList.value = emptyList()
                _onuList.value = emptyList()

                _dashboardData.value = _dashboardData.value.copy(
                    deviceName = current.name,
                    ip = current.ip,
                    snmpPort = current.snmpPort,
                    status = "offline",
                    cpuPercent = 0,
                    ramPercent = 0,
                    tempCelsius = 0,
                    totalSubscribers = 0,
                    onlineSubscribers = 0,
                    onlinePercent = 0.0,
                    powerOffSubscribers = 0,
                    powerOffPercent = 0.0,
                    wiredCutSubscribers = 0,
                    wiredCutPercent = 0.0,
                    activePonCount = 0,
                    totalPonCount = current.totalPonPorts,
                    ponStatusText = "অসংযুক্ত (Offline)",
                    ponDescription = "${current.ip}:${current.snmpPort} এ রেসপন্স পাওয়া যায়নি। OLT বা নেটওয়ার্ক কানেকশন যাচাই করুন।",
                    totalBars = current.totalPonPorts,
                    activeBars = 0
                )
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
                _selectedOlt.value = null
                _dashboardData.value = DashboardData(
                    deviceName = "কোনো OLT সংযুক্ত নেই",
                    ip = "0.0.0.0",
                    status = "offline",
                    ponDescription = "OLT কানেক্ট করতে 'OLT ডিভাইস' ট্যাব ব্যবহার করুন"
                )
                _ponList.value = emptyList()
                _onuList.value = emptyList()
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

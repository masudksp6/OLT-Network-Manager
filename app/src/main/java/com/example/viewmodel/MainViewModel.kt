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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // --- ১. থিম কন্ট্রোল (ডার্ক / লাইট) ---
    // ডিফল্টভাবে সম্পূর্ণ লাইট/সাদা মোড (false)
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

    // --- নোটিফিকেশন স্টেট ---
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun dismissNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }

    // --- ২. একক JSON ফাইল থেকে লোড হওয়া ড্যাশবোর্ড ডেটা ---
    private val _dashboardData = MutableStateFlow(DashboardData())
    val dashboardData: StateFlow<DashboardData> = _dashboardData.asStateFlow()

    private val _cpuUsage = MutableStateFlow(25)
    val cpuUsage: StateFlow<Int> = _cpuUsage.asStateFlow()

    private val _ramUsage = MutableStateFlow(16)
    val ramUsage: StateFlow<Int> = _ramUsage.asStateFlow()

    private val _temperature = MutableStateFlow(54)
    val temperature: StateFlow<Int> = _temperature.asStateFlow()

    private val _uptime = MutableStateFlow("1d 9h 21m 53s")
    val uptime: StateFlow<String> = _uptime.asStateFlow()

    // --- ৩. PON পোর্ট ডেটা (JSON থেকে লোড হওয়া) ---
    private val _ponList = MutableStateFlow<List<PonInterface>>(emptyList())
    val ponList: StateFlow<List<PonInterface>> = _ponList.asStateFlow()

    // --- ৪. ONU ডেটা (JSON থেকে লোড হওয়া) ---
    private val _onuList = MutableStateFlow<List<OnuItem>>(emptyList())
    val onuList: StateFlow<List<OnuItem>> = _onuList.asStateFlow()

    // --- ৫. OLT ডিভাইস ডেটা (JSON থেকে লোড হওয়া) ---
    private val _oltList = MutableStateFlow<List<OltItem>>(emptyList())
    val oltList: StateFlow<List<OltItem>> = _oltList.asStateFlow()

    // --- সক্রিয় OLT সিলেকশন স্টেট ---
    private val _selectedOlt = MutableStateFlow<OltItem?>(null)
    val selectedOlt: StateFlow<OltItem?> = _selectedOlt.asStateFlow()

    private val _isRefreshingLive = MutableStateFlow(false)
    val isRefreshingLive: StateFlow<Boolean> = _isRefreshingLive.asStateFlow()

    init {
        loadDataFromJson()
        startTelemetrySimulation()
    }

    /**
     * assets/network_data.json থেকে সমস্ত ডেমো ডেটা লোড করার মেথড
     */
    fun loadDataFromJson() {
        val appData = NetworkDataLoader.loadData(getApplication(), "network_data.json")
        _oltList.value = loadPersistedOlts(appData.oltList)
        _notifications.value = appData.notifications

        if (_oltList.value.isNotEmpty()) {
            if (_selectedOlt.value == null) {
                _selectedOlt.value = _oltList.value.first()
            }
        } else {
            _selectedOlt.value = null
            _dashboardData.value = DashboardData()
            _cpuUsage.value = 0
            _ramUsage.value = 0
            _temperature.value = 0
            _uptime.value = "0d 0h 0m 0s"
            _ponList.value = emptyList()
            _onuList.value = emptyList()
        }
    }

    fun selectOlt(olt: OltItem) {
        _selectedOlt.value = olt
        _dashboardData.value = _dashboardData.value.copy(
            deviceName = olt.name,
            ip = olt.ip,
            snmpPort = olt.snmpPort,
            status = olt.status
        )
        refreshActiveOltData()
    }

    /**
     * সরাসরি OLT IP ও SNMP পোর্ট দিয়ে লাইভ ডেটা কুয়েরি করা (টেলিমেট্রি, PON ও ONU টেবিল)
     */
    fun refreshActiveOltData() {
        val current = _selectedOlt.value ?: return
        viewModelScope.launch {
            _isRefreshingLive.value = true
            val fullSync = DirectSnmpClient.fetchRealOltFullSync(
                ip = current.ip,
                port = current.snmpPort,
                community = current.community,
                vendor = current.vendor
            )
            if (fullSync != null) {
                val realData = fullSync.telemetry
                _cpuUsage.value = realData.cpuPercent
                _ramUsage.value = realData.ramPercent
                _temperature.value = realData.tempCelsius
                _uptime.value = realData.uptime

                if (fullSync.ponList.isNotEmpty()) {
                    _ponList.value = fullSync.ponList
                }

                if (fullSync.onuList.isNotEmpty()) {
                    _onuList.value = fullSync.onuList
                }

                // লাইভ ONU স্ট্যাটাস থেকে পরিসংখ্যান ক্যালকুলেট
                val currentOnus = if (fullSync.onuList.isNotEmpty()) fullSync.onuList else _onuList.value
                val totalCount = currentOnus.size.coerceAtLeast(1)
                val onlineCount = currentOnus.count { !it.rx.contains("-3") && !it.rx.contains("-4") }
                val onlinePct = (onlineCount.toDouble() / totalCount * 100.0)

                val activePons = if (fullSync.ponList.isNotEmpty()) {
                    fullSync.ponList.count { it.status == "optimal" || it.status == "active" }
                } else _dashboardData.value.activePonCount

                val totalPons = if (fullSync.ponList.isNotEmpty()) fullSync.ponList.size else _dashboardData.value.totalPonCount

                _dashboardData.value = _dashboardData.value.copy(
                    deviceName = realData.deviceName,
                    ip = current.ip,
                    snmpPort = current.snmpPort,
                    uptime = realData.uptime,
                    status = realData.status,
                    cpuPercent = realData.cpuPercent,
                    ramPercent = realData.ramPercent,
                    tempCelsius = realData.tempCelsius,
                    totalSubscribers = currentOnus.size,
                    onlineSubscribers = onlineCount,
                    onlinePercent = Math.round(onlinePct * 10.0) / 10.0,
                    activePonCount = activePons,
                    totalPonCount = totalPons,
                    ponStatusText = if (activePons == totalPons && totalPons > 0) "100% চালু ↑" else "$activePons/$totalPons সক্রিয়",
                    ponDescription = "সবক ${activePons}টি পোর্ট কার্যকর আছে",
                    activeBars = activePons,
                    totalBars = totalPons.coerceAtLeast(8)
                )
            }
            _isRefreshingLive.value = false
        }
    }

    /**
     * সরাসরি OLT কানেকশন টেস্ট (সরাসরি UDP 161 দিয়ে)
     */
    suspend fun testOlt(ip: String, port: Int, community: String): SnmpTestResult {
        return DirectSnmpClient.testConnection(ip, port, community)
    }

    private fun startTelemetrySimulation() {
        viewModelScope.launch {
            while (true) {
                delay(8000)
                if (!_isRefreshingLive.value && _selectedOlt.value != null) {
                    _cpuUsage.value = Random.nextInt(22, 28)
                    _ramUsage.value = Random.nextInt(15, 18)
                    _temperature.value = Random.nextInt(52, 56)
                } else if (_selectedOlt.value == null) {
                    _cpuUsage.value = 0
                    _ramUsage.value = 0
                    _temperature.value = 0
                }
            }
        }
    }

    // --- OLT CRUD অপারেশনস ও স্থায়ী সংরক্ষণ ---
    fun addOlt(olt: OltItem) {
        val updated = _oltList.value + olt
        _oltList.value = updated
        persistOlts(updated)
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
        if (_selectedOlt.value?.id == id && updated.isNotEmpty()) {
            selectOlt(updated.first())
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
                jsonArray.put(obj)
            }
            prefs.edit().putString("saved_olts", jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadPersistedOlts(defaultList: List<OltItem>): List<OltItem> {
        try {
            val prefs = getApplication<Application>().getSharedPreferences("app_olts_store", Context.MODE_PRIVATE)
            val saved = prefs.getString("saved_olts", null) ?: return defaultList
            val jsonArray = JSONArray(saved)
            if (jsonArray.length() == 0) return defaultList
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
                        vendor = obj.optString("vendor", "vsol"),
                        snmpPort = obj.optInt("snmpPort", 161)
                    )
                )
            }
            return list
        } catch (e: Exception) {
            return defaultList
        }
    }
}


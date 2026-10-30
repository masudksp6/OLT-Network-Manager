package com.example.data.repository

import android.content.Context
import com.example.data.model.DashboardData
import com.example.data.model.NotificationItem
import com.example.data.model.OltItem
import com.example.data.model.OnuItem
import com.example.data.model.PonInterface
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

data class AppNetworkData(
    val dashboard: DashboardData,
    val ponList: List<PonInterface>,
    val onuList: List<OnuItem>,
    val oltList: List<OltItem>,
    val notifications: List<NotificationItem> = emptyList()
)

object NetworkDataLoader {

    fun loadData(context: Context, fileName: String = "network_data.json"): AppNetworkData {
        try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            val root = JSONObject(jsonString)

            // ১. ড্যাশবোর্ড ডেটা পার্সিং
            val dashObj = root.optJSONObject("dashboard") ?: JSONObject()
            val dashboard = DashboardData(
                deviceName = dashObj.optString("deviceName", "V.SOL V1600D4-DP"),
                ip = dashObj.optString("ip", "180.94.28.100"),
                snmpPort = dashObj.optInt("snmpPort", 161),
                uptime = dashObj.optString("uptime", "1d 9h 21m 53s"),
                status = dashObj.optString("status", "online"),
                cpuPercent = dashObj.optInt("cpuPercent", 25),
                ramPercent = dashObj.optInt("ramPercent", 16),
                tempCelsius = dashObj.optInt("tempCelsius", 54),
                totalSubscribers = dashObj.optInt("totalSubscribers", 63),
                onlineSubscribers = dashObj.optInt("onlineSubscribers", 60),
                onlinePercent = dashObj.optDouble("onlinePercent", 95.2),
                powerOffSubscribers = dashObj.optInt("powerOffSubscribers", 0),
                powerOffPercent = dashObj.optDouble("powerOffPercent", 0.0),
                wiredCutSubscribers = dashObj.optInt("wiredCutSubscribers", 3),
                wiredCutPercent = dashObj.optDouble("wiredCutPercent", 4.8),
                activePonCount = dashObj.optInt("activePonCount", 4),
                totalPonCount = dashObj.optInt("totalPonCount", 4),
                ponStatusText = dashObj.optString("ponStatusText", "100% চালু ↑"),
                ponDescription = dashObj.optString("ponDescription", "সবক 4টি EPON পোর্ট কার্যকর আছে"),
                totalBars = dashObj.optInt("totalBars", 8),
                activeBars = dashObj.optInt("activeBars", 4)
            )

            // ২. PON ইন্টারফেস তালিকা পার্সিং
            val ponArray = root.optJSONArray("ponInterfaces")
            val ponList = mutableListOf<PonInterface>()
            if (ponArray != null) {
                for (i in 0 until ponArray.length()) {
                    val p = ponArray.getJSONObject(i)
                    ponList.add(
                        PonInterface(
                            id = p.optString("id", "EPON0/${i + 1}"),
                            type = p.optString("type", "EPON PX20++"),
                            status = p.optString("status", "optimal"),
                            txPower = p.optString("txPower", "+9.19 dBm"),
                            sfpTemp = p.optString("sfpTemp", "36.1°C"),
                            voltage = p.optString("voltage", "3.26V"),
                            laserBias = p.optString("laserBias", "26.0 mA"),
                            connected = p.optInt("connected", 0),
                            maxOnu = p.optInt("maxOnu", 64),
                            online = p.optInt("online", 0),
                            powerOff = p.optInt("powerOff", 0),
                            wiredCut = p.optInt("wiredCut", 0)
                        )
                    )
                }
            }

            // ৩. ONU তালিকা পার্সিং
            val onuArray = root.optJSONArray("onuList")
            val onuList = mutableListOf<OnuItem>()
            if (onuArray != null) {
                for (i in 0 until onuArray.length()) {
                    val o = onuArray.getJSONObject(i)
                    onuList.add(
                        OnuItem(
                            id = o.optInt("id", i + 1),
                            user = o.optString("user", "User-${i + 1}"),
                            status = o.optString("status", "online"),
                            fdb = o.optString("fdb", "99.8%"),
                            ip = o.optString("ip", "-"),
                            uptime = o.optString("uptime", "-"),
                            port = o.optString("port", "EPON0/1:${i + 1}"),
                            sn = o.optString("sn", "SN%08X".format(i + 1)),
                            rx = o.optString("rx", "-20.0 dBm"),
                            tx = o.optString("tx", "+2.0 dBm"),
                            distance = o.optString("distance", "140 m")
                        )
                    )
                }
            }

            // ৪. OLT তালিকা পার্সিং
            val oltArray = root.optJSONArray("oltList")
            val oltList = mutableListOf<OltItem>()
            if (oltArray != null) {
                for (i in 0 until oltArray.length()) {
                    val ol = oltArray.getJSONObject(i)
                    oltList.add(
                        OltItem(
                            id = ol.optLong("id", (i + 1).toLong()),
                            name = ol.optString("name", "OLT-${i + 1}"),
                            ip = ol.optString("ip", "192.168.1.1"),
                            location = ol.optString("location", "Main POP"),
                            user = ol.optString("user", "admin"),
                            snmpVersion = ol.optString("snmpVersion", "v2c"),
                            community = ol.optString("community", "public"),
                            status = ol.optString("status", "online"),
                            vendor = ol.optString("vendor", if (ol.optString("name").contains("bdcom", ignoreCase = true)) "bdcom" else "vsol"),
                            snmpPort = ol.optInt("snmpPort", 161)
                        )
                    )
                }
            }

            // ৫. নোটিফিকেশন পার্সিং
            val notifArray = root.optJSONArray("notifications")
            val notifList = mutableListOf<NotificationItem>()
            if (notifArray != null) {
                for (i in 0 until notifArray.length()) {
                    val no = notifArray.getJSONObject(i)
                    notifList.add(
                        NotificationItem(
                            id = no.optString("id", "notif_$i"),
                            type = no.optString("type", "info"),
                            title = no.optString("title", ""),
                            message = no.optString("message", ""),
                            timeAgo = no.optString("timeAgo", ""),
                            isRead = no.optBoolean("isRead", false)
                        )
                    )
                }
            }

            return AppNetworkData(
                dashboard = dashboard,
                ponList = ponList,
                onuList = onuList,
                oltList = oltList,
                notifications = notifList
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // কোনো কারণে ফাইলে ত্রুটি হলে ডিফল্ট ডেটা ফেরত দেওয়া হবে
            return AppNetworkData(
                dashboard = DashboardData(),
                ponList = emptyList(),
                onuList = emptyList(),
                oltList = emptyList(),
                notifications = emptyList()
            )
        }
    }
}

package com.example.data.repository

import android.content.Context
import com.example.data.model.DashboardData
import com.example.data.model.NotificationItem
import com.example.data.model.OltItem
import com.example.data.model.OnuItem
import com.example.data.model.PonInterface

data class AppNetworkData(
    val dashboard: DashboardData,
    val ponList: List<PonInterface>,
    val onuList: List<OnuItem>,
    val oltList: List<OltItem>,
    val notifications: List<NotificationItem> = emptyList()
)

object NetworkDataLoader {

    fun getDefaultData(): AppNetworkData {
        return AppNetworkData(
            dashboard = DashboardData(),
            ponList = emptyList(),
            onuList = emptyList(),
            oltList = emptyList(),
            notifications = emptyList()
        )
    }

    fun loadData(context: Context, fileName: String = ""): AppNetworkData {
        return getDefaultData()
    }
}

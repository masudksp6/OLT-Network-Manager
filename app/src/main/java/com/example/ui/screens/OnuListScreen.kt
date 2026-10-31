package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OnuItem
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun OnuListScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val ponList by viewModel.ponList.collectAsState()
    val onuList by viewModel.onuList.collectAsState()
    val selectedOlt by viewModel.selectedOlt.collectAsState()
    val isRefreshingLive by viewModel.isRefreshingLive.collectAsState()

    // OLT থেকে আসা আসল ফিজিক্যাল পোর্ট তালিকা (সম্পূর্ণ ডাইনামিক)
    val availablePorts = remember(ponList) {
        ponList.map { it.id }
    }

    var selectedPort by remember { mutableStateOf(availablePorts.firstOrNull() ?: "") }

    // যখন OLT পরিবর্তন হয় তখন সিলেক্টেড পোর্ট আপডেট করা
    LaunchedEffect(availablePorts) {
        if (selectedPort !in availablePorts) {
            selectedPort = availablePorts.firstOrNull() ?: "EPON0/1"
        }
    }

    var selectedFilter by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // নির্বাচিত পোর্টের আন্ডারে থাকা গ্রাহক ডিভাইসসমূহ
    val portOnus = remember(onuList, selectedPort) {
        onuList.filter { it.port.startsWith(selectedPort) }
    }

    val totalCount = portOnus.size
    val onlineCount = portOnus.count { it.status.equals("online", ignoreCase = true) }
    val wireCutCount = portOnus.count { it.status.equals("wirecut", ignoreCase = true) }
    val powerOffCount = portOnus.count { it.status.equals("poweroff", ignoreCase = true) }

    // সার্চ এবং ফিল্টার অনুযায়ী তালিকা
    val filteredList = remember(portOnus, selectedFilter, searchQuery) {
        portOnus.filter { onu ->
            val matchesFilter = when (selectedFilter) {
                "online" -> onu.status.equals("online", ignoreCase = true)
                "wirecut" -> onu.status.equals("wirecut", ignoreCase = true)
                "poweroff" -> onu.status.equals("poweroff", ignoreCase = true)
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                    onu.user.contains(searchQuery, ignoreCase = true) ||
                    onu.sn.contains(searchQuery, ignoreCase = true) ||
                    onu.mac.contains(searchQuery, ignoreCase = true) ||
                    onu.ip.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("onu_list_screen")
    ) {
        // --- হেডার ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (selectedPort.isNotBlank()) "ONU তালিকা: $selectedPort" else "ONU তালিকা",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                val oltName = selectedOlt?.name ?: "কোনো OLT সংযুক্ত নেই"
                Text(
                    text = if (selectedOlt != null) "$oltName | মোট $totalCount গ্রাহক ডিভাইস" else "OLT ডিভাইস ট্যাব থেকে OLT নির্বাচন বা যুক্ত করুন",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // রিফ্রেশ বাটন
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(BluePrimary.copy(alpha = 0.12f))
                        .clickable { viewModel.refreshActiveOltData() }
                        .testTag("onu_refresh_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRefreshingLive) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = BluePrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh from OLT",
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // সার্চ টগল বাটন
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { isSearchActive = !isSearchActive }
                        .testTag("onu_search_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search ONU",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // সার্চ বার
        if (isSearchActive) {
            Spacer(modifier = Modifier.height(10.dp))
            GlobalSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "ইউজারনেম, SN, MAC বা IP দিয়ে খুঁজুন...",
                testTag = "onu_search_input"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- সম্পূর্ণ ডাইনামিক PON পোর্ট সিলেক্টর (OLT-এর ৪, ৮ বা ১৬টি পোর্ট অনুযায়ী স্বয়ংক্রিয়ভাবে স্ক্রলেবল) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            availablePorts.forEach { port ->
                val isSelected = selectedPort == port
                val portCount = onuList.count { it.port.startsWith(port) }
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedPort = port }
                        .testTag("port_selector_$port"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) GreenOnline else MaterialTheme.colorScheme.surface,
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                        }
                        Text(
                            text = if (portCount > 0) "$port ($portCount)" else port,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- নির্বাচিত পোর্টের পরিসংখ্যান কাউন্টার (Total, Online, Wire Cut, Power Off) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlobalStatCounterCard(
                count = "$totalCount",
                label = "মোট গ্রাহক",
                containerColor = MaterialTheme.colorScheme.surface,
                textColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                testTag = "stat_total_onu"
            )
            GlobalStatCounterCard(
                count = "$onlineCount",
                label = "অনলাইন",
                containerColor = GreenBadgeBg,
                textColor = GreenText,
                modifier = Modifier.weight(1f),
                testTag = "stat_online_onu"
            )
            GlobalStatCounterCard(
                count = "$wireCutCount",
                label = "ওয়্যার কাট",
                containerColor = RedBadgeBg,
                textColor = RedWiredCut,
                modifier = Modifier.weight(1f),
                testTag = "stat_wirecut_onu"
            )
            GlobalStatCounterCard(
                count = "$powerOffCount",
                label = "পাওয়ার অফ",
                containerColor = YellowBadgeBg,
                textColor = YellowPowerOff,
                modifier = Modifier.weight(1f),
                testTag = "stat_poweroff_onu"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- ফিল্টার পিলস (All, Online, Wire Cut, Power Off) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlobalFilterButton(
                text = "সবাই",
                badgeCount = totalCount,
                isSelected = selectedFilter == "all",
                onClick = { selectedFilter = "all" },
                testTag = "filter_onu_all"
            )
            GlobalFilterButton(
                text = "অনলাইন",
                badgeCount = onlineCount,
                isSelected = selectedFilter == "online",
                onClick = { selectedFilter = "online" },
                activeColor = GreenOnline,
                testTag = "filter_onu_online"
            )
            GlobalFilterButton(
                text = "ওয়্যার কাট",
                badgeCount = wireCutCount,
                isSelected = selectedFilter == "wirecut",
                onClick = { selectedFilter = "wirecut" },
                activeColor = RedWiredCut,
                testTag = "filter_onu_wirecut"
            )
            GlobalFilterButton(
                text = "পাওয়ার অফ",
                badgeCount = powerOffCount,
                isSelected = selectedFilter == "poweroff",
                onClick = { selectedFilter = "poweroff" },
                activeColor = YellowPowerOff,
                testTag = "filter_onu_poweroff"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- গ্রাহক ONU তালিকা ---
        if (selectedOlt == null || availablePorts.isEmpty()) {
            GlobalEmptyState(
                icon = Icons.Default.Person,
                title = "কোনো OLT সংযুক্ত নেই",
                subtitle = "OLT ডিভাইস মেনু থেকে একটি OLT যুক্ত বা নির্বাচন করুন"
            )
        } else if (filteredList.isEmpty()) {
            GlobalEmptyState(
                icon = Icons.Default.Person,
                title = "কোনো ONU পাওয়া যায়নি",
                subtitle = "এই পোর্টের অধীনে ফিল্টার অনুযায়ী কোনো গ্রাহক ডিভাইস সক্রিয় নেই"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList, key = { it.id }) { onu ->
                    OnuClientCard(
                        onu = onu,
                        onCopySn = { sn ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ONU SN", sn)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "SN কপি করা হয়েছে: $sn", Toast.LENGTH_SHORT).show()
                        },
                        onCopyMac = { mac ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ONU MAC", mac)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "MAC কপি করা হয়েছে: $mac", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

/**
 * গ্রাহক প্রান্তের ONU ডিভাইস কার্ড:
 * শুধুমাত্র গ্রাহক সংক্রান্ত ডেটা ধারণ করে (BDCOM স্ট্যান্ডার্ড ফরম্যাট: 53-shanto, EPON0/1:1, MAC, SN, Rx, Tx, Distance, Uptime)
 */
@Composable
fun OnuClientCard(
    onu: OnuItem,
    onCopySn: (String) -> Unit,
    onCopyMac: (String) -> Unit
) {
    val isOnline = onu.status.equals("online", ignoreCase = true)
    val isWireCut = onu.status.equals("wirecut", ignoreCase = true)

    val iconColor = if (isOnline) GreenOnline else MaterialTheme.colorScheme.onSurfaceVariant

    GlobalCard(
        cornerRadius = 16.dp,
        elevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        testTag = "onu_client_${onu.id}"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // গ্রাহকের নাম ও স্ট্যাটাস ব্যাজ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = onu.user,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                GlobalStatusBadge(status = onu.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // FDB স্ট্যাটাস, IP ও আপটাইম
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BluePrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "FDB ${onu.fdb}",
                        color = BluePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = "IP: ${onu.ip} • আপটাইম: ${onu.uptime}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BDCOM স্ট্যান্ডার্ড পোর্ট ও ONU ID (যেমন EPON0/1:1), MAC ও SN
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "#${onu.id}",
                        color = BluePrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = onu.port, // BDCOM ফরম্যাট: EPON0/1:1
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "• SN: ${onu.sn}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onCopySn(onu.sn) },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy SN",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "MAC: ${onu.mac}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(10.dp))

            // সিগন্যাল মেট্রিক্স (Rx Signal, Tx Power, দূরত্ব)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rx সিগন্যাল
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Rx সিগন্যাল", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = onu.rx,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOnline) GreenOnline else RedWiredCut
                        )
                    }

                    // Tx পাওয়ার
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tx পাওয়ার", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = onu.tx,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // দূরত্ব (Distance)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("দূরত্ব", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = onu.distance,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PonInterface
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun PonScreen(viewModel: MainViewModel) {
    val ponList by viewModel.ponList.collectAsState()
    val selectedOlt by viewModel.selectedOlt.collectAsState()
    val isRefreshingLive by viewModel.isRefreshingLive.collectAsState()
    var selectedFilter by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val optimalCount = ponList.count { it.status == "optimal" }
    val downCount = ponList.count { it.status == "down" }
    val degradedCount = 0

    val filteredList = remember(ponList, selectedFilter, searchQuery) {
        ponList.filter { pon ->
            val matchesFilter = when (selectedFilter) {
                "optimal" -> pon.status == "optimal"
                "down" -> pon.status == "down"
                "degraded" -> false
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                    pon.id.contains(searchQuery, ignoreCase = true) ||
                    pon.type.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("pon_screen")
    ) {
        // --- ১. হেডার রো (স্ক্রিনশটের অনুরূপ) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PON Interfaces (${ponList.size})",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                val oltLabel = selectedOlt?.let { "${it.name} • ${it.ip}" } ?: "V.SOL V1600D4-DP • 180.94.28.100"
                Text(
                    text = oltLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // লাইভ রিফ্রেশ বাটন
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2FE))
                        .clickable { viewModel.refreshActiveOltData() }
                        .testTag("pon_refresh_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRefreshingLive) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF0284C7)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh from OLT",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // সার্চ বাটন
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .clickable { isSearchActive = !isSearchActive }
                        .testTag("pon_search_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search PON",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // সার্চ বার উন্মুক্ত থাকলে প্রদর্শন
        if (isSearchActive) {
            Spacer(modifier = Modifier.height(10.dp))
            GlobalSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "PON পোর্ট খুঁজুন (উদা: EPON0/1)...",
                testTag = "pon_search_input"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- ২. ফিল্টার পিলস (All, Optimal, Degraded, Down) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PonFilterPill(
                label = "All (${ponList.size})",
                isSelected = selectedFilter == "all",
                onClick = { selectedFilter = "all" },
                activeColor = Color(0xFF0284C7),
                dotColor = null
            )

            PonFilterPill(
                label = "Optimal ( $optimalCount )",
                isSelected = selectedFilter == "optimal",
                onClick = { selectedFilter = "optimal" },
                activeColor = Color(0xFF0284C7),
                dotColor = Color(0xFF10B981)
            )

            PonFilterPill(
                label = "Degraded ( $degradedCount )",
                isSelected = selectedFilter == "degraded",
                onClick = { selectedFilter = "degraded" },
                activeColor = Color(0xFF0284C7),
                dotColor = Color(0xFFF59E0B)
            )

            PonFilterPill(
                label = "Down ( $downCount )",
                isSelected = selectedFilter == "down",
                onClick = { selectedFilter = "down" },
                activeColor = Color(0xFF0284C7),
                dotColor = Color(0xFFEF4444)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- ৩. PON ইন্টারফেস কার্ড তালিকা ---
        if (filteredList.isEmpty()) {
            GlobalEmptyState(
                icon = Icons.Default.Router,
                title = "কোনো PON পোর্ট পাওয়া যায়নি",
                subtitle = "ফিল্টার বা সার্চ পরিবর্তন করে দেখুন"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredList, key = { it.id }) { pon ->
                    PonInterfaceCard(pon = pon)
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun PonFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    activeColor: Color,
    dotColor: Color?
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (isSelected) activeColor else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else dotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun PonInterfaceCard(pon: PonInterface) {
    val isDown = pon.status.equals("down", ignoreCase = true)
    val cardBorder = if (isDown) {
        BorderStroke(1.5.dp, Color(0xFFFDE68A)) // নরম হলুদ বর্ডার
    } else {
        BorderStroke(1.dp, Color(0xFFE2E8F0))
    }

    GlobalCard(
        cornerRadius = 18.dp,
        elevation = 1.dp,
        border = cardBorder,
        testTag = "pon_card_${pon.id}"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // শীর্ষ রো: আইকন, নাম, PX ব্যাজ ও স্ট্যাটাস
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
                        imageVector = Icons.Default.AccountTree,
                        contentDescription = "Port",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = pon.id,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A)
                    )

                    // EPON PX... লাইট ব্লু ব্যাজ
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFE0F2FE)
                    ) {
                        Text(
                            text = "EPON PX...",
                            color = Color(0xFF0284C7),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // স্ট্যাটাস ইনডিকেটর (✓ Active অথবা ▲ Port Down)
                if (isDown) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Port Down",
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Port Down",
                            color = Color(0xFFEA580C),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Active",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Active",
                            color = Color(0xFF16A34A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // অপটিক্যাল টেলিমেট্রি গ্রিড (Tx Power, SFP Temp, Voltage, Laser Bias)
            GlobalOpticalTelemetryGrid(
                txPower = pon.txPower,
                sfpTemp = pon.sfpTemp,
                voltage = pon.voltage,
                laserBias = pon.laserBias
            )

            Spacer(modifier = Modifier.height(16.dp))

            // সংযুক্ত ONU ডোনাট এবং বিবরণ সেকশন
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // মিনি ডোনাট
                GlobalMiniDonut(
                    centerText = if (isDown) "0" else "${pon.online}",
                    hasTraffic = !isDown,
                    wireCutSlice = pon.wiredCut > 0,
                    size = 58.dp,
                    strokeWidth = 7.dp
                )

                // ডানের বিবরণ ও ৩টি স্ট্যাটাস
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Connected: ${if (isDown) 0 else pon.connected} / ${pon.maxOnu} ONUs",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Online
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Online", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${if (isDown) 0 else pon.online}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        // Power Off
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF59E0B))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Power Off", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${if (isDown) 0 else pon.powerOff}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        // Wired Cut
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Wired Cut", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${if (isDown) 0 else pon.wiredCut}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }
        }
    }
}

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

    val optimalCount = ponList.count { it.status.equals("optimal", ignoreCase = true) || it.status.equals("active", ignoreCase = true) }
    val degradedCount = ponList.count { it.status.equals("degraded", ignoreCase = true) }
    val downCount = ponList.count { it.status.equals("down", ignoreCase = true) }

    // ফিল্টার এবং সার্চ লজিক
    val filteredList = remember(ponList, selectedFilter, searchQuery) {
        ponList.filter { pon ->
            val matchesFilter = when (selectedFilter) {
                "optimal" -> pon.status.equals("optimal", ignoreCase = true) || pon.status.equals("active", ignoreCase = true)
                "degraded" -> pon.status.equals("degraded", ignoreCase = true)
                "down" -> pon.status.equals("down", ignoreCase = true)
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
                    text = "PON Interfaces (${ponList.size})",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(2.dp))
                val oltLabel = selectedOlt?.let { "${it.name} • ${it.ip} (${it.vendor.uppercase()})" }
                    ?: "কোনো OLT সংযুক্ত নেই"
                Text(
                    text = oltLabel,
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
                        .testTag("pon_refresh_btn"),
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
                        .testTag("pon_search_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search PON",
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
                placeholder = "PON পোর্ট নাম দিয়ে খুঁজুন (যেমন: EPON0/1)...",
                testTag = "pon_search_input"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- ফিল্টার ট্যাব (All, Optimal, Degraded, Down) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlobalFilterButton(
                text = "All",
                badgeCount = ponList.size,
                isSelected = selectedFilter == "all",
                onClick = { selectedFilter = "all" },
                testTag = "filter_pon_all"
            )
            GlobalFilterButton(
                text = "Optimal",
                badgeCount = optimalCount,
                isSelected = selectedFilter == "optimal",
                onClick = { selectedFilter = "optimal" },
                activeColor = GreenOnline,
                testTag = "filter_pon_optimal"
            )
            GlobalFilterButton(
                text = "Degraded",
                badgeCount = degradedCount,
                isSelected = selectedFilter == "degraded",
                onClick = { selectedFilter = "degraded" },
                activeColor = YellowPowerOff,
                testTag = "filter_pon_degraded"
            )
            GlobalFilterButton(
                text = "Down",
                badgeCount = downCount,
                isSelected = selectedFilter == "down",
                onClick = { selectedFilter = "down" },
                activeColor = RedWiredCut,
                testTag = "filter_pon_down"
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- ফিজিক্যাল PON পোর্ট তালিকা ---
        if (selectedOlt == null || ponList.isEmpty()) {
            GlobalEmptyState(
                icon = Icons.Default.Router,
                title = "কোনো PON পোর্ট সংযুক্ত নেই",
                subtitle = "OLT ডিভাইস ট্যাব থেকে একটি OLT যুক্ত বা নির্বাচন করুন"
            )
        } else if (filteredList.isEmpty()) {
            GlobalEmptyState(
                icon = Icons.Default.Router,
                title = "কোনো PON পোর্ট পাওয়া যায়নি",
                subtitle = "ফিল্টার পরিবর্তন করে আবার চেষ্টা করুন"
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
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

/**
 * ফিজিক্যাল PON পোর্ট কার্ড:
 * শুধুমাত্র ডিস্ট্রিবিউশন/SFP প্যারামিটার ও ওই পোর্টের অধীনস্থ ONU সামারি অনুপাত ডিসপ্লে করে।
 */
@Composable
fun PonInterfaceCard(pon: PonInterface) {
    val isDown = pon.status.equals("down", ignoreCase = true)
    val isDegraded = pon.status.equals("degraded", ignoreCase = true)

    val cardBorder = when {
        isDown -> BorderStroke(1.5.dp, Color(0xFFFECACA))
        isDegraded -> BorderStroke(1.5.dp, Color(0xFFFDE68A))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
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
            // পোর্ট নাম, টাইপ ও স্ট্যাটাস
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
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = pon.id,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = BluePrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = pon.type,
                            color = BluePrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // স্ট্যাটাস ব্যাজ (Optimal / Degraded / Port Down)
                when {
                    isDown -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Port Down",
                                tint = RedWiredCut,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Port Down",
                                color = RedWiredCut,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    isDegraded -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Degraded",
                                tint = YellowPowerOff,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Degraded",
                                color = YellowPowerOff,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Optimal",
                                tint = GreenOnline,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Optimal",
                                color = GreenOnline,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // অপটিক্যাল মেট্রিক্স গ্রিড (Tx Power, SFP Temp, Voltage, Laser Bias)
            GlobalOpticalTelemetryGrid(
                txPower = pon.txPower,
                sfpTemp = pon.sfpTemp,
                voltage = pon.voltage,
                laserBias = pon.laserBias
            )

            Spacer(modifier = Modifier.height(16.dp))

            // সংযুক্ত ONU অনুপাত ও স্ট্যাটাস সামারি
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // মিনি ডোন্ট চার্ট
                GlobalMiniDonut(
                    centerText = if (isDown) "0" else "${pon.online}",
                    hasTraffic = !isDown,
                    wireCutSlice = pon.wiredCut > 0,
                    size = 58.dp,
                    strokeWidth = 7.dp
                )

                // পরিসংখ্যান বিবরণী
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Connected: ${if (isDown) 0 else pon.connected} / ${pon.maxOnu} ONUs",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
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
                                        .background(GreenOnline)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Online", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${if (isDown) 0 else pon.online}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Power Off
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(YellowPowerOff)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Power Off", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${if (isDown) 0 else pon.powerOff}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Wired Cut
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(RedWiredCut)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Wired Cut", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${if (isDown) 0 else pon.wiredCut}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val dashData by viewModel.dashboardData.collectAsState()
    val cpu by viewModel.cpuUsage.collectAsState()
    val ram by viewModel.ramUsage.collectAsState()
    val temp by viewModel.temperature.collectAsState()
    val uptime by viewModel.uptime.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val selectedOlt by viewModel.selectedOlt.collectAsState()
    val oltList by viewModel.oltList.collectAsState()

    var showNotificationDrawer by remember { mutableStateOf(false) }
    var showOltPicker by remember { mutableStateOf(false) }

    val totalOnu = dashData.totalSubscribers
    val onlineOnu = dashData.onlineSubscribers
    val wireCutOnu = dashData.wiredCutSubscribers
    val powerOffOnu = dashData.powerOffSubscribers
    val onlinePercent = dashData.onlinePercent
    val wireCutPercent = dashData.wiredCutPercent
    val powerOffPercent = dashData.powerOffPercent

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- হেডার ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .clickable { showOltPicker = true }
                        .padding(vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PulsingDot(isActive = selectedOlt != null && dashData.status == "online")
                        Text(
                            text = selectedOlt?.name ?: dashData.deviceName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Select OLT",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    val activeIp = selectedOlt?.ip ?: dashData.ip
                    val activePort = selectedOlt?.snmpPort ?: dashData.snmpPort
                    Text(
                        text = if (selectedOlt != null) "$activeIp • SNMP $activePort" else "কোনো ডিভাইস নির্বাচন করা নেই",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // নোটিফিকেশন বেল
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BluePrimary.copy(alpha = 0.12f))
                            .clickable { showNotificationDrawer = true }
                            .testTag("notification_bell_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        val hasUnread = notifications.any { !it.isRead }
                        if (hasUnread) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 9.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(RedWiredCut)
                            )
                        }
                    }
                }
            }

            // --- কন্টেন্ট সেকশন ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ১. ডিভাইস পরিচিতি কার্ড
                GlobalCard(
                    cornerRadius = 18.dp,
                    elevation = 1.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    testTag = "device_info_card"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val isConnected = selectedOlt != null && dashData.status == "online"
                            val vendorName = selectedOlt?.vendor?.uppercase() ?: "OLT"
                            val vendorBg = if (isConnected) GreenBadgeBg else MaterialTheme.colorScheme.surfaceVariant
                            val vendorColor = if (isConnected) GreenText else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(vendorBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = vendorName,
                                    color = vendorColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            }

                            Column {
                                Text(
                                    text = dashData.deviceName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (isConnected) "আপটাইম: $uptime" else "স্ট্যাটাস: অসংযুক্ত (Offline)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // স্ট্যাটাস
                        if (selectedOlt != null && dashData.status == "online") {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = GreenBadgeBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(GreenOnline)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "অনলাইন",
                                        color = GreenText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "অসংযুক্ত",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // যদি কোনো OLT সক্রিয় না থাকে, তবে একটি অ্যাকশন কার্ড প্রদর্শন করা
                if (selectedOlt == null) {
                    GlobalCard(
                        cornerRadius = 16.dp,
                        elevation = 1.dp,
                        border = BorderStroke(1.dp, BluePrimary.copy(alpha = 0.3f)),
                        testTag = "no_olt_action_card"
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "কোনো OLT সক্রিয় নেই",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "টেলিমোট্রি ও গ্রাহক ডেটা দেখতে একটি OLT যুক্ত করুন বা নির্বাচন করুন",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showOltPicker = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("OLT নির্বাচন বা যুক্ত করুন", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ২. সিস্টেম টেলিমোট্রি কার্ড (CPU, RAM, Temp)
                GlobalCard(
                    cornerRadius = 18.dp,
                    elevation = 1.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    testTag = "telemetry_card"
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "সিস্টেম টেলিমোট্রি",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val isConnected = selectedOlt != null && dashData.status == "online"
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isConnected) GreenBadgeBg else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (isConnected) GreenOnline else MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isConnected) "স্বাভাবিক" else "অফলাইন",
                                        color = if (isConnected) GreenText else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlobalArcGauge(
                                value = "$cpu%",
                                label = "CPU ব্যবহার",
                                subLabel = if (cpu > 0) "Optimal" else "0.0%",
                                progress = cpu / 100f,
                                arcColor = BluePrimary,
                                subLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            GlobalArcGauge(
                                value = "$ram%",
                                label = "RAM ব্যবহার",
                                subLabel = "$ram.0%",
                                progress = ram / 100f,
                                arcColor = BluePrimary,
                                subLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            GlobalArcGauge(
                                value = "$temp°C",
                                label = "তাপমাত্রা",
                                subLabel = if (temp > 50) "উষ্ণ" else if (temp > 0) "স্বাভাবিক" else "0°C",
                                progress = temp / 100f,
                                arcColor = OrangeGauge,
                                subLabelColor = OrangeGauge
                            )
                        }
                    }
                }

                // ৩. ONU গ্রাহক বিন্যাস চার্ট
                GlobalCard(
                    cornerRadius = 18.dp,
                    elevation = 1.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    testTag = "onu_distribution_card"
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ONU গ্রাহক ডিভাইস বিন্যাস",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$totalOnu মোট গ্রাহক",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            GlobalDonutChart(
                                percentage = onlinePercent.toInt(),
                                size = 104.dp,
                                strokeWidth = 12.dp
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // অনলাইন
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(GreenOnline)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "অনলাইন",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "$onlineOnu (%.1f%%)".format(onlinePercent),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenText
                                    )
                                }

                                // পাওয়ার অফ
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(YellowPowerOff)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "পাওয়ার অফ",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "$powerOffOnu (%.1f%%)".format(powerOffPercent),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = YellowPowerOff
                                    )
                                }

                                // ওয়্যার কাট
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(RedWiredCut)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "ওয়্যার কাট",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "$wireCutOnu (%.1f%%)".format(wireCutPercent),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RedWiredCut
                                    )
                                }
                            }
                        }
                    }
                }

                // ৪. অ্যাক্টিভ PON পোর্ট স্ট্যাটাস কার্ড
                GlobalCard(
                    cornerRadius = 18.dp,
                    elevation = 1.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    testTag = "pon_status_card"
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "সক্রিয় PON ইন্টারফেস",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "${dashData.activePonCount}",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (dashData.activePonCount > 0) GreenText else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (dashData.activePonCount > 0) GreenBadgeBg else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = dashData.ponStatusText,
                                            color = if (dashData.activePonCount > 0) GreenText else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dashData.ponDescription,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // পোর্ট বার ইন্ডিকেটর
                            GlobalPortBarIndicator(
                                activeCount = dashData.activeBars,
                                totalCount = dashData.totalBars,
                                activeColor = BluePrimary,
                                barWidth = 6.dp,
                                barHeight = 30.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // নোটিফিকেশন ড্রয়ার
        NotificationDrawer(
            isOpen = showNotificationDrawer,
            notifications = notifications,
            onClose = { showNotificationDrawer = false },
            onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() }
        )

        // OLT সিলেক্টর ডায়ালগ
        if (showOltPicker) {
            GlobalDialog(
                title = "সক্রিয় OLT নির্বাচন করুন",
                confirmButtonText = "ঠিক আছে",
                confirmButtonColor = BluePrimary,
                onConfirm = { showOltPicker = false },
                onDismissRequest = { showOltPicker = false }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (oltList.isEmpty()) {
                        Text(
                            text = "কোনো OLT তালিকাভুক্ত নেই। 'OLT ডিভাইস' মেনু থেকে আপনার OLT যুক্ত করুন।",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "পোর্ট পর্যবেক্ষণ ও টেলিমেট্রির জন্য OLT নির্বাচন করুন:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        oltList.forEach { olt ->
                            val isSel = selectedOlt?.id == olt.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) BluePrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.5.dp, if (isSel) BluePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectOlt(olt)
                                        showOltPicker = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = olt.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = BluePrimary.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = "${olt.totalPonPorts} PORTS",
                                                    color = BluePrimary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${olt.ip}:${olt.snmpPort} • ${olt.location}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSel) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = BluePrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PulsingDot(isActive: Boolean = true) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(10.dp)
            .graphicsLayer { this.alpha = if (isActive) alpha else 1f }
            .clip(CircleShape)
            .background(if (isActive) GreenOnline else Color(0xFF94A3B8))
    )
}

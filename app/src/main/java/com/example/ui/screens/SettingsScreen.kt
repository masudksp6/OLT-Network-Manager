package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.BluePrimary
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val autoRefresh by viewModel.autoRefreshEnabled.collectAsState()
    val snmpTimeoutMs by viewModel.snmpTimeoutMs.collectAsState()
    val snmpRetries by viewModel.snmpRetries.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("settings_screen")
    ) {
        // --- গ্লোবাল অ্যাপ হেডার ---
        GlobalAppHeader(
            title = "সেটিংস ও কনফিগারেশন",
            subtitle = "অ্যাপ থিম এবং SNMP ট্র্যাকিং প্যারামিটারসমূহ",
            testTag = "settings_app_header"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- থিম সেটিংস ---
            GlobalCard(testTag = "theme_settings_card") {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "অ্যাপ্লিকেশন থিম",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme == true) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ডার্ক মোড",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isDarkTheme == true) "ডার্ক থিম সক্রিয়" else "লাইট থিম সক্রিয়",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isDarkTheme == true,
                        onCheckedChange = { checked ->
                            viewModel.setDarkMode(checked)
                        },
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- SNMP প্যারামিটারস ---
        GlobalCard(testTag = "snmp_config_card") {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SNMP ও পোলিং কনফিগারেশন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "লাইভ ব্যাকগ্রাউন্ড সিঙ্ক",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "CPU, RAM এবং PON অবস্থা ৮ সেকেন্ড পরপর আপডেট",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoRefresh,
                        onCheckedChange = { viewModel.setAutoRefresh(it) },
                        modifier = Modifier.testTag("auto_refresh_switch")
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                SettingRowItem(
                    icon = Icons.Default.Timer,
                    title = "SNMP টাইমআউট",
                    value = "$snmpTimeoutMs ms"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                SettingRowItem(
                    icon = Icons.Default.Refresh,
                    title = "রিট্রাই সংখ্যা",
                    value = "$snmpRetries বার"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- অ্যাপ পরিচিতি ---
        GlobalCard(testTag = "about_card") {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "অ্যাপ পরিচিতি",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BluePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "OLT Manager Pro v1.0",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ISP OLT & PON/ONU Real-Time Monitor via SNMP",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
}

@Composable
fun SettingRowItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = BluePrimary
        )
    }
}

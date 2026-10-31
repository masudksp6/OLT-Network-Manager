package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NetworkCheck
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
import com.example.data.model.OltItem
import com.example.network.snmp.SnmpTestResult
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun OltListScreen(viewModel: MainViewModel) {
    val oltList by viewModel.oltList.collectAsState()
    val selectedOlt by viewModel.selectedOlt.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingOlt by remember { mutableStateOf<OltItem?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deleteTargetOlt by remember { mutableStateOf<OltItem?>(null) }

    val filteredList = remember(oltList, searchQuery) {
        if (searchQuery.isBlank()) {
            oltList
        } else {
            oltList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.ip.contains(searchQuery, ignoreCase = true) ||
                        it.location.contains(searchQuery, ignoreCase = true) ||
                        it.vendor.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("olt_list_screen")
    ) {
        // --- গ্লোবাল অ্যাপ হেডার ---
        GlobalAppHeader(
            title = "OLT ডিভাইস তালিকা",
            subtitle = "সংরক্ষিত ডিভাইস: ${oltList.size} টি (BDCOM, V-SOL)",
            showActiveIndicator = selectedOlt != null,
            isActive = selectedOlt != null,
            actions = {
                // নতুন OLT যুক্ত করার বাটন
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BluePrimary)
                        .clickable {
                            editingOlt = null
                            showAddEditDialog = true
                        }
                        .testTag("add_olt_fab"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add OLT",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            testTag = "olt_app_header"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // --- সার্চ বার ---
        GlobalSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "OLT নাম, IP বা লোকেশন দিয়ে খুঁজুন...",
            testTag = "olt_search_bar"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- OLT কার্ড তালিকা ---
        if (filteredList.isEmpty()) {
            GlobalEmptyState(
                icon = Icons.Default.Dns,
                title = "কোনো OLT ডিভাইস পাওয়া যায়নি",
                subtitle = "নতুন OLT যুক্ত করতে ওপরের + বাটনে চাপুন"
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredList, key = { it.id }) { olt ->
                    val isSelected = selectedOlt?.id == olt.id
                    OltDeviceCard(
                        olt = olt,
                        isSelected = isSelected,
                        onSelect = { viewModel.selectOlt(olt) },
                        onEdit = {
                            editingOlt = olt
                            showAddEditDialog = true
                        },
                        onDelete = {
                            deleteTargetOlt = olt
                            showDeleteConfirmDialog = true
                        },
                        onTestConnection = { ip, port, community ->
                            viewModel.testOlt(ip, port, community)
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

    // --- OLT যুক্ত বা এডিট করার ডায়ালগ ---
    if (showAddEditDialog) {
        OltAddEditModal(
            existingOlt = editingOlt,
            onDismiss = { showAddEditDialog = false },
            onSave = { olt ->
                if (editingOlt == null) {
                    viewModel.addOlt(olt)
                } else {
                    viewModel.updateOlt(olt)
                }
                showAddEditDialog = false
            },
            onTestConnection = { ip, port, community ->
                viewModel.testOlt(ip, port, community)
            }
        )
    }

    // --- ডিলিট কনফার্মেশন ডায়ালগ ---
    if (showDeleteConfirmDialog && deleteTargetOlt != null) {
        GlobalDialog(
            title = "OLT মুছে ফেলা",
            confirmButtonText = "মুছে ফেলুন",
            confirmButtonColor = RedWiredCut,
            onConfirm = {
                viewModel.deleteOlt(deleteTargetOlt!!.id)
                showDeleteConfirmDialog = false
                deleteTargetOlt = null
            },
            onDismissRequest = {
                showDeleteConfirmDialog = false
                deleteTargetOlt = null
            }
        ) {
            Text(
                text = "আপনি কি নিশ্চিতভাবে '${deleteTargetOlt!!.name}' OLT ডিভাইসটি ডিলিট করতে চান?",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OltDeviceCard(
    olt: OltItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestConnection: suspend (String, Int, String) -> SnmpTestResult
) {
    val coroutineScope = rememberCoroutineScope()
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<SnmpTestResult?>(null) }

    val vendorColor = when (olt.vendor.lowercase()) {
        "vsol" -> BluePrimary
        "bdcom" -> GreenOnline
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    GlobalCard(
        cornerRadius = 16.dp,
        elevation = if (isSelected) 3.dp else 1.dp,
        border = BorderStroke(
            1.5.dp,
            if (isSelected) BluePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        testTag = "olt_card_${olt.id}"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // শিরোনাম ও ভেন্ডর ট্যাগ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = olt.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = vendorColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${olt.vendor.uppercase()} (${olt.totalPonPorts}P)",
                                color = vendorColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "লোকেশন: ${olt.location}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isSelected) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = BluePrimary.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = BluePrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "সক্রিয় OLT",
                                color = BluePrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // আইপি ও SNMP কনফিগারেশন বক্স
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "IP: ${olt.ip}:${olt.snmpPort}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ইউজার: ${olt.user}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SNMP সংস্করণ: ${olt.snmpVersion}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Community: ${olt.community}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // টেস্ট রেজাল্ট প্রদর্শনী
            if (testResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = if (testResult!!.success) GreenBadgeBg else RedBadgeBg,
                    border = BorderStroke(1.dp, if (testResult!!.success) Color(0xFFBBF7D0) else Color(0xFFFECACA))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = testResult!!.message,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (testResult!!.success) GreenText else RedWiredCut
                        )
                        if (testResult!!.sysDescr != null) {
                            Text(
                                text = "বর্ণনা: ${testResult!!.sysDescr}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // অ্যাকশন বাটনস
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SNMP টেস্ট বাটন
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            isTesting = true
                            testResult = onTestConnection(olt.ip, olt.snmpPort, olt.community)
                            isTesting = false
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    border = BorderStroke(1.dp, BluePrimary),
                    modifier = Modifier.height(34.dp)
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = BluePrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isTesting) "টেস্ট হচ্ছে..." else "SNMP টেস্ট",
                        color = BluePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isSelected) {
                        Button(
                            onClick = onSelect,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = "সক্রিয় করুন",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, RedWiredCut),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = RedWiredCut,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OltAddEditModal(
    existingOlt: OltItem?,
    onDismiss: () -> Unit,
    onSave: (OltItem) -> Unit,
    onTestConnection: suspend (String, Int, String) -> SnmpTestResult
) {
    val coroutineScope = rememberCoroutineScope()
    var vendor by remember { mutableStateOf(existingOlt?.vendor ?: "bdcom") }
    var name by remember { mutableStateOf(existingOlt?.name ?: if (vendor == "bdcom") "BDCOM GP3600-08" else "V.SOL V1600D4-DP") }
    var ip by remember { mutableStateOf(existingOlt?.ip ?: "") }
    var snmpPortStr by remember { mutableStateOf(existingOlt?.snmpPort?.toString() ?: "161") }
    var location by remember { mutableStateOf(existingOlt?.location ?: "") }
    var snmpVersion by remember { mutableStateOf(existingOlt?.snmpVersion ?: "v2c") }
    var community by remember { mutableStateOf(existingOlt?.community ?: "public") }
    var totalPonPortsStr by remember { mutableStateOf(existingOlt?.totalPonPorts?.toString() ?: "8") }

    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<SnmpTestResult?>(null) }

    GlobalDialog(
        title = if (existingOlt == null) "নতুন OLT যুক্ত করুন" else "OLT সেটিংস আপডেট",
        confirmButtonText = "সংরক্ষণ করুন",
        confirmButtonColor = BluePrimary,
        onConfirm = {
            if (name.isNotBlank() && ip.isNotBlank()) {
                val port = snmpPortStr.toIntOrNull() ?: 161
                val ponCount = totalPonPortsStr.toIntOrNull() ?: 8
                val olt = OltItem(
                    id = existingOlt?.id ?: System.currentTimeMillis(),
                    name = name.trim(),
                    ip = ip.trim(),
                    location = if (location.isBlank()) "মূল ডাটা সেন্টার" else location.trim(),
                    user = "admin",
                    snmpVersion = snmpVersion.trim(),
                    community = community.trim(),
                    status = existingOlt?.status ?: "online",
                    vendor = vendor,
                    snmpPort = port,
                    totalPonPorts = ponCount
                )
                onSave(olt)
            }
        },
        onDismissRequest = onDismiss
    ) {
        // ভেন্ডর নির্বাচন
        Text(
            text = "OLT ব্র্যান্ড/ভেন্ডর নির্বাচন করুন:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val vendors = listOf("bdcom" to "BDCOM OLT", "vsol" to "V-SOL OLT", "generic" to "অন্যান্য")
            vendors.forEach { (vKey, vLabel) ->
                val isSel = vendor == vKey
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) BluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (isSel) BluePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable {
                            vendor = vKey
                            if (existingOlt == null) {
                                name = when (vKey) {
                                    "bdcom" -> "BDCOM GP3600-08"
                                    "vsol" -> "V.SOL V1600D4-DP"
                                    else -> "Generic OLT"
                                }
                                totalPonPortsStr = when (vKey) {
                                    "bdcom" -> "8"
                                    "vsol" -> "4"
                                    else -> "8"
                                }
                            }
                        }
                ) {
                    Text(
                        text = vLabel,
                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        GlobalInput(
            value = name,
            onValueChange = { name = it },
            label = "OLT নাম বা মডেল",
            placeholder = "যেমন: BDCOM GP3600-08"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(2.2f)) {
                GlobalInput(
                    value = ip,
                    onValueChange = { ip = it },
                    label = "ম্যানেজমেন্ট IP",
                    placeholder = "যেমন: 192.168.1.100"
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                GlobalInput(
                    value = totalPonPortsStr,
                    onValueChange = { totalPonPortsStr = it },
                    label = "PON পোর্ট",
                    placeholder = "4, 8 বা 16"
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1.5f)) {
                GlobalInput(
                    value = community,
                    onValueChange = { community = it },
                    label = "SNMP Community",
                    placeholder = "public"
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                GlobalInput(
                    value = snmpPortStr,
                    onValueChange = { snmpPortStr = it },
                    label = "SNMP Port",
                    placeholder = "161"
                )
            }
        }

        GlobalInput(
            value = location,
            onValueChange = { location = it },
            label = "ইনস্টলেশন লোকেশন / POP",
            placeholder = "যেমন: ধানমন্ডি মেইন পিওপি"
        )

        // লাইভ টেস্ট বাটন
        OutlinedButton(
            onClick = {
                if (ip.isNotBlank()) {
                    coroutineScope.launch {
                        isTesting = true
                        val port = snmpPortStr.toIntOrNull() ?: 161
                        testResult = onTestConnection(ip.trim(), port, community.trim())
                        isTesting = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, BluePrimary)
        ) {
            if (isTesting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = BluePrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("সংযোগ যাচাই করা হচ্ছে...", color = BluePrimary, fontSize = 12.sp)
            } else {
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("লাইভ OLT সংযোগ পরীক্ষা (UDP 161)", color = BluePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (testResult != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = if (testResult!!.success) GreenBadgeBg else RedBadgeBg,
                border = BorderStroke(1.dp, if (testResult!!.success) Color(0xFFBBF7D0) else Color(0xFFFECACA))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = testResult!!.message,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (testResult!!.success) GreenText else RedWiredCut
                    )
                    if (testResult!!.sysDescr != null) {
                        Text(
                            text = "বর্ণনা: ${testResult!!.sysDescr}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

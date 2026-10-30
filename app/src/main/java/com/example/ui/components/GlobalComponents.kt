package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Global Reusable Card with solid background and clean borders
 * to prevent any transparency or screen ghosting.
 */
@Composable
fun GlobalCard(
    modifier: Modifier = Modifier,
    testTag: String = "global_card",
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 1.dp,
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick)
    } else {
        modifier
            .fillMaxWidth()
            .testTag(testTag)
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = border,
        content = content
    )
}

/**
 * Global Reusable Search Bar with Clear action and search icon
 */
@Composable
fun GlobalSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "ইউজারনেম, SN বা IP খুঁজুন...",
    modifier: Modifier = Modifier,
    testTag: String = "global_search_bar"
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Global Reusable Filter Chip / Button
 */
@Composable
fun GlobalFilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "filter_button_$text",
    badgeCount: Int? = null,
    activeColor: Color = MaterialTheme.colorScheme.primary
) {
    val containerColor = if (isSelected) {
        activeColor
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isSelected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(50),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        modifier = modifier
            .height(34.dp)
            .testTag(testTag)
    ) {
        Text(
            text = if (badgeCount != null) "$text ($badgeCount)" else text,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/**
 * Global Reusable Metric Circle for Telemetry (CPU, RAM, Temp)
 */
@Composable
fun GlobalMetricCircle(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Global Reusable Stat Box (Counter indicator)
 */
@Composable
fun GlobalStatBox(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Global Reusable Status Badge (Online, Wire Cut, Power Off)
 */
@Composable
fun GlobalStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (status.lowercase()) {
        "online" -> Pair("✓ অনলাইন", GreenOnline)
        "wirecut" -> Pair("✗ ওয়্যার কাট", RedWiredCut)
        "poweroff" -> Pair("⚠ পাওয়ার অফ", YellowPowerOff)
        "optimal" -> Pair("✓ সক্রিয়", GreenOnline)
        "down" -> Pair("⚠ ডাউন", RedWiredCut)
        else -> Pair(status, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Global Reusable Input Field
 */
@Composable
fun GlobalInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    testTag: String = "global_input_$label"
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        placeholder = { Text(placeholder, fontSize = 12.sp) },
        leadingIcon = leadingIcon?.let {
            { Icon(it, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Global Reusable Dialog Component
 */
@Composable
fun GlobalDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmButtonText: String = "নিশ্চিত করুন",
    onConfirm: () -> Unit,
    dismissButtonText: String = "বাতিল",
    confirmButtonColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                content()
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = confirmButtonColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(confirmButtonText, color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismissRequest,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    dismissButtonText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

/**
 * Global Empty State Component
 */
@Composable
fun GlobalEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Global Circular Gauge matching the screenshot telemetry style:
 * Background track with progress arc, center value, bottom title and subtitle.
 */
@Composable
fun GlobalArcGauge(
    value: String,
    label: String,
    subLabel: String,
    progress: Float,
    arcColor: Color,
    subLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    strokeWidth: Dp = 6.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = strokeWidth.toPx()
                val arcSize = androidx.compose.ui.geometry.Size(
                    this.size.width - strokePx,
                    this.size.height - strokePx
                )
                val topLeft = androidx.compose.ui.geometry.Offset(strokePx / 2, strokePx / 2)

                // Background track
                drawArc(
                    color = Color(0xFFF1F5F9),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx)
                )

                // Foreground active arc
                val sweep = (progress.coerceIn(0f, 1f)) * 360f
                drawArc(
                    color = arcColor,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = subLabelColor
        )
    }
}

/**
 * Global Donut Chart matching the ONU Subscriber Distribution in the screenshot:
 * Large 95% center text, thick green progress ring, tiny red cut section at top.
 */
@Composable
fun GlobalDonutChart(
    percentage: Int = 95,
    modifier: Modifier = Modifier,
    size: Dp = 104.dp,
    strokeWidth: Dp = 11.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val arcSize = androidx.compose.ui.geometry.Size(
                this.size.width - strokePx,
                this.size.height - strokePx
            )
            val topLeft = androidx.compose.ui.geometry.Offset(strokePx / 2, strokePx / 2)

            val redSweep = 18f
            val greenSweep = 360f - redSweep

            // Wire cut red slice at the top (-90 degrees)
            drawArc(
                color = RedWiredCut,
                startAngle = -90f,
                sweepAngle = redSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Green online arc
            drawArc(
                color = GreenOnline,
                startAngle = -90f + redSweep,
                sweepAngle = greenSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Text(
            text = "$percentage%",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Global Port Bar Indicator showing vertical pill bars for active and inactive ports
 * (e.g., 4 blue active bars and 4 gray inactive bars in a row).
 */
@Composable
fun GlobalPortBarIndicator(
    activeCount: Int = 4,
    totalCount: Int = 8,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF0284C7),
    inactiveColor: Color = Color(0xFFE2E8F0),
    barWidth: Dp = 6.dp,
    barHeight: Dp = 28.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalCount) {
            val isActive = i <= activeCount
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(barHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isActive) activeColor else inactiveColor)
            )
        }
    }
}

/**
 * Global Mini Counter Card (used in ONU screen 4-stat row: মোট, অনলাইন, ওয়্যার কাট, পাওয়ার অফ)
 */
@Composable
fun GlobalStatCounterCard(
    count: String,
    label: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    testTag: String = "stat_card_$label"
) {
    Surface(
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = count,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

/**
 * Global Mini Donut for PON Interface cards:
 * Displays active connected count in the center with green + red slice ring.
 */
@Composable
fun GlobalMiniDonut(
    centerText: String,
    hasTraffic: Boolean = true,
    wireCutSlice: Boolean = true,
    size: Dp = 56.dp,
    strokeWidth: Dp = 6.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val arcSize = androidx.compose.ui.geometry.Size(
                this.size.width - strokePx,
                this.size.height - strokePx
            )
            val topLeft = androidx.compose.ui.geometry.Offset(strokePx / 2, strokePx / 2)

            if (!hasTraffic) {
                // Down / Inactive port - gray ring
                drawArc(
                    color = Color(0xFFF1F5F9),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx)
                )
            } else {
                val redSweep = if (wireCutSlice) 30f else 0f
                val greenSweep = 360f - redSweep

                if (redSweep > 0f) {
                    drawArc(
                        color = RedWiredCut,
                        startAngle = -90f,
                        sweepAngle = redSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }

                drawArc(
                    color = GreenOnline,
                    startAngle = -90f + redSweep,
                    sweepAngle = greenSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Text(
            text = centerText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Global Optical Telemetry Grid (4 columns: Tx Power, SFP Temp, Voltage, Laser Bias)
 */
@Composable
fun GlobalOpticalTelemetryGrid(
    txPower: String,
    sfpTemp: String,
    voltage: String,
    laserBias: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8FAFC))
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tx Power", fontSize = 10.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(2.dp))
            Text(txPower, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenOnline)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SFP Temp", fontSize = 10.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(2.dp))
            Text(sfpTemp, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Voltage", fontSize = 10.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(2.dp))
            Text(voltage, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Laser Bias", fontSize = 10.sp, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(2.dp))
            Text(laserBias, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
        }
    }
}



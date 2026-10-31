package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
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

/**
 * Pulsing Status Dot for indicating live connection or active device
 */
@Composable
fun PulsingDot(
    isActive: Boolean = true,
    activeColor: Color = GreenOnline,
    inactiveColor: Color = Color(0xFF94A3B8),
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .size(9.dp)
            .graphicsLayer { this.alpha = if (isActive) alpha else 1f }
            .clip(CircleShape)
            .background(if (isActive) activeColor else inactiveColor)
    )
}

/**
 * Global App Header Component
 * Standardized, clean, and elevated header used across all screens.
 * Supports consistent height, title, subtitle, optional active dot,
 * clickable title (for dropdown OLT selection), and trailing actions.
 */
@Composable
fun GlobalAppHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showActiveIndicator: Boolean = false,
    isActive: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    onTitleClick: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    testTag: String = "global_app_header"
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .then(
                        if (onTitleClick != null) {
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onTitleClick)
                                .padding(vertical = 2.dp, horizontal = 2.dp)
                        } else Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (showActiveIndicator) {
                            PulsingDot(isActive = isActive)
                        }

                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (onTitleClick != null) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Dropdown",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (!subtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (actions != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actions()
                }
            }
        }
    }
}

/**
 * Global Export / Report Dialog
 * Allows field technicians to view, copy, and share structured reports.
 */
@Composable
fun GlobalExportDialog(
    title: String,
    reportText: String,
    onDismissRequest: () -> Unit,
    onCopyText: () -> Unit,
    onShareText: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "global_export_dialog"
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier.testTag(testTag),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = BluePrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                Text(
                    text = "রিপোর্টের প্রিভিউ (ক্লিপবোর্ডে কপি বা শেয়ার করুন):",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        val scrollState = androidx.compose.foundation.rememberScrollState()
                        Text(
                            text = reportText,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.verticalScroll(scrollState)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onShareText,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("dialog_share_btn")
            ) {
                Text("শেয়ার করুন", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCopyText,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("dialog_copy_btn")
                ) {
                    Text("কপি করুন", fontSize = 13.sp)
                }
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.testTag("dialog_close_btn")
                ) {
                    Text("বন্ধ করুন", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}

/**
 * Global Quick Diagnostic Modal for single ONU test
 * Evaluates optical signal strength, wire status, and technician recommendations.
 */
@Composable
fun GlobalQuickDiagnosticDialog(
    onuUser: String,
    port: String,
    sn: String,
    mac: String,
    status: String,
    rxPower: String,
    txPower: String,
    distance: String,
    isChecking: Boolean,
    onReCheck: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "global_diagnostic_dialog"
) {
    val isOnline = status.equals("online", ignoreCase = true)
    val isWireCut = status.equals("wirecut", ignoreCase = true)

    // সিগন্যাল হেলথ বিশ্লেষণ
    val rxNum = rxPower.replace("dBm", "").trim().toDoubleOrNull()
    val signalHealth = when {
        !isOnline && isWireCut -> "ওয়্যার কাট (ফাইবার অপটিক বিচ্ছিন্ন)"
        !isOnline -> "পাওয়ার অফ (গ্রাহক রাউটার/ONU বন্ধ)"
        rxNum != null && rxNum > -15.0 -> "সিগন্যাল খুব বেশি হাই (High Power Warning)"
        rxNum != null && rxNum >= -24.0 -> "সিগন্যাল স্বাস্থ্য: চমৎকার (Optimal: -18 to -24 dBm)"
        rxNum != null && rxNum >= -27.0 -> "সিগন্যাল স্বাস্থ্য: সহনশীল (Marginal: -24 to -27 dBm)"
        rxNum != null -> "সিগন্যাল স্বাস্থ্য: অত্যন্ত দুর্বল (Critical: < -27 dBm)"
        else -> "সিগন্যাল তথ্য অপ্রতুল"
    }

    val recommendation = when {
        !isOnline && isWireCut -> "লাইন পরামর্শ: ফাইবার ড্রপ তারে কাটা বা জয়েন্টে ভাঁজ পড়েছে। OTDR দিয়ে দূরত্ব মেপে ফাইবার স্প্লাইসিং করুন।"
        !isOnline -> "লাইন পরামর্শ: গ্রাহকের বিদ্যুৎ সংযোগ বা এডাপ্টার চেক করতে বলুন।"
        rxNum != null && rxNum < -27.0 -> "লাইন পরামর্শ: প্যাচকর্ড অতিরিক্ত বাঁকা বা স্প্লিটার কাপলারে ময়লা আছে। ক্লিনিং বা স্প্লিটার পোর্ট পরিবর্তন করুন।"
        else -> "লাইন পরামর্শ: ফাইবার সিগন্যাল ও ডাটা ট্রাফিক স্বাভাবিক রয়েছে। কোনো অ্যাকশন প্রয়োজন নেই।"
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier.testTag(testTag),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isOnline) GreenBadgeBg else RedBadgeBg,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        GlobalStatusBadge(status = status)
                    }
                }
                Column {
                    Text(
                        text = "ONU লাইভ ডায়াগনস্টিক",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$onuUser ($port)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // অপটিক্যাল সিগন্যাল কার্ড
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Rx সিগন্যাল", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = rxPower,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) GreenOnline else RedWiredCut
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tx পাওয়ার", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = txPower,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("দূরত্ব", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = distance,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                        }
                    }
                }

                // হেলথ স্ট্যাটাস বক্স
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isOnline) GreenBadgeBg.copy(alpha = 0.5f) else RedBadgeBg.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, (if (isOnline) GreenOnline else RedWiredCut).copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = signalHealth,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isOnline) GreenText else RedWiredCut
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = recommendation,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // ডিভাইস পরিচিতি (SN & MAC)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("সিরিয়াল নাম্বার (SN): $sn", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("ম্যাক এড্রেস (MAC): $mac", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onReCheck,
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(8.dp),
                enabled = !isChecking,
                modifier = Modifier.testTag("diagnostic_retest_btn")
            ) {
                if (isChecking) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("যাচাই হচ্ছে...", fontSize = 12.sp)
                } else {
                    Text("আবার টেস্ট করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.testTag("diagnostic_close_btn")
            ) {
                Text("বন্ধ করুন", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

/**
 * Global Notification Bell Button with live unread counter badge.
 * Reusable across headers in Dashboard, Pon, ONU, etc.
 */
@Composable
fun GlobalNotificationBell(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "notification_bell_btn"
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(BluePrimary.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications",
            tint = BluePrimary,
            modifier = Modifier.size(20.dp)
        )
        if (unreadCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp),
                shape = CircleShape,
                color = RedWiredCut
            ) {
                Text(
                    text = if (unreadCount > 9) "9+" else "$unreadCount",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/**
 * Global Port Traffic Bar showing live Uplink/Downlink throughput for PON ports.
 */
@Composable
fun GlobalPortTrafficBar(
    rxMbps: Double,
    txMbps: Double,
    maxCapacityMbps: Double = 1250.0,
    modifier: Modifier = Modifier,
    testTag: String = "port_traffic_bar"
) {
    val rxPercent = (rxMbps / maxCapacityMbps).toFloat().coerceIn(0f, 1f)
    val txPercent = (txMbps / maxCapacityMbps).toFloat().coerceIn(0f, 1f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "পোর্ট লাইভ থ্রুপুট ব্যান্ডউইথ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${String.format(java.util.Locale.US, "%.1f", rxMbps + txMbps)} Mbps / 1.25 Gbps",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ডাউনলোড (Downlink)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "RX (ডাউন):",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(55.dp)
                )
                LinearProgressIndicator(
                    progress = { rxPercent },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = GreenOnline,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "${String.format(java.util.Locale.US, "%.1f", rxMbps)}M",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // আপলোড (Uplink)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "TX (আপ):",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(55.dp)
                )
                LinearProgressIndicator(
                    progress = { txPercent },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BluePrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "${String.format(java.util.Locale.US, "%.1f", txMbps)}M",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}






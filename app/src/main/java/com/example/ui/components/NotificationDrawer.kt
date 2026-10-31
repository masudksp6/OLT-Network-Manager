package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationItem

/**
 * স্ক্রিনশটের হুবহু নোটিফিকেশন ড্রয়ার কম্পোনেন্ট
 */
@Composable
fun NotificationDrawer(
    isOpen: Boolean,
    notifications: List<NotificationItem>,
    onClose: () -> Unit,
    onMarkAllAsRead: () -> Unit,
    onDismissNotification: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("notification_drawer_overlay")
    ) {
        // --- পেছনের আবছা/স্ক্রিম ব্যাকগ্রাউন্ড ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClose
                )
        )

        // --- ডানপাশ থেকে ড্রয়ার প্যানেল ---
        AnimatedVisibility(
            visible = isOpen,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.78f)
                    .shadow(16.dp),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    // --- ১. হেডার: নীল বেল, টাইটেল, এবং ক্রস বাটন ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "নোটিফিকেশন",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "নোটিফিকেশন",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("close_notifications_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        thickness = 1.dp
                    )

                    // --- ২. নোটিফিকেশন আইটেম তালিকা ---
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (notifications.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "কোনো নতুন নোটিফিকেশন নেই",
                                        fontSize = 14.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        } else {
                            items(notifications, key = { it.id }) { item ->
                                NotificationCard(
                                    item = item,
                                    onDismiss = { onDismissNotification(item.id) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        thickness = 1.dp
                    )

                    // --- ৩. ফুটার: "সব পড়া হয়েছে হিসেবে মার্ক করুন" ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "সব পড়া হয়েছে হিসেবে মার্ক করুন",
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .clickable { onMarkAllAsRead() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("mark_all_read_btn")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onDismiss: () -> Unit
) {
    val isError = item.type.equals("error", ignoreCase = true)

    val bgColor = if (isError) Color(0xFFFEF2F2) else Color(0xFFEFF6FF)
    val borderColor = if (isError) Color(0xFFFEE2E2) else Color(0xFFDBEAFE)
    val iconTint = if (isError) Color(0xFFEF4444) else Color(0xFF0284C7)
    val icon = if (isError) Icons.Default.Warning else Icons.Default.Info

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notif_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = item.type,
                tint = iconTint,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 1.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.message,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.timeAgo,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

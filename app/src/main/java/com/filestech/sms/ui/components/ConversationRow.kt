package com.filestech.sms.ui.components

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filestech.sms.domain.model.Conversation

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
)
@Composable
fun ConversationRow(
    conversation: Conversation,
    modifier: Modifier = Modifier,
    index: Int = 0,
    count: Int = 1,
    shapes: ListItemShapes = ListItemDefaults.segmentedShapes(index = index, count = count),
    colors: ListItemColors = ListItemDefaults.segmentedColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ),
    subtitleNumber: String? = null,
    showAvatars: Boolean = true,
    previewLines: Int = 1,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    val cs = MaterialTheme.colorScheme
    val isUnread = conversation.unreadCount > 0
    val hasDraft = !conversation.draft.isNullOrBlank()

    SegmentedListItem(
        onClick = onClick,
        onLongClick = onLongClick,
        shapes = shapes,
        colors = colors,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        leadingContent = if (showAvatars) {
            {
                MinimalAvatar(isGroup = conversation.isGroup)
            }
        } else null,
        overlineContent = if (subtitleNumber != null) {
            {
                Text(
                    text = subtitleNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else null,
        supportingContent = {
            val previewText = when {
                hasDraft -> conversation.draft
                else -> conversation.lastMessagePreview.orEmpty()
            }
            Text(
                text = previewText.orEmpty(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isUnread) FontWeight.Medium else FontWeight.Normal,
                    fontStyle = if (hasDraft) FontStyle.Italic else FontStyle.Normal,
                    lineHeight = 20.sp,
                ),
                color = when {
                    hasDraft -> cs.error
                    isUnread -> cs.onSurface
                    else -> cs.onSurfaceVariant
                },
                maxLines = previewLines,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
            ) {
                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = cs.error,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                } else if (conversation.lastMessageAt > 0L) {
                    Text(
                        text = formatTimestamp(conversation.lastMessageAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUnread) cs.primary else cs.onSurfaceVariant,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                    )
                }

                if (conversation.pinned || conversation.muted || isUnread) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (conversation.pinned) {
                            Icon(
                                imageVector = Icons.Outlined.PushPin,
                                contentDescription = null,
                                tint = cs.onSurfaceVariant,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                        if (conversation.muted) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsOff,
                                contentDescription = null,
                                tint = cs.onSurfaceVariant,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                        if (isUnread) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(cs.primary),
                            )
                        }
                    }
                }
            }
        },
    ) {
        // Main Headline (Conversation Title)
        Text(
            text = conversation.title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 16.sp,
            ),
            color = cs.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Clean, minimal, non-gradient avatar with solid surfaceContainerHigh tint
 * and monochrome vector icon.
 */
@Composable
fun MinimalAvatar(
    isGroup: Boolean,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = CircleShape,
        color = cs.surfaceContainerHigh,
        modifier = modifier.size(46.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (isGroup) Icons.Outlined.Group else Icons.Outlined.Person,
                contentDescription = null,
                tint = cs.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()
}
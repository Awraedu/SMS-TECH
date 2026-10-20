package com.filestech.sms.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------
// Design tokens
// ---------------------------------------------------------

private val NavigationSurfaceColor = Color(0xFFF9F9FB)
private val NavigationSelectedColor = Color(0xFFE0E0E2)
private val NavigationActiveContent = Color(0xFF080808)
private val NavigationInactiveContent = Color(0xFF858585)
private val NavigationBadgeColor = Color(0xFFE95A15)
private val NavigationBadgeContent = Color.White

private val NavigationHeight = 58.dp
private val NavigationPreferredWidth = 224.dp
private val NavigationInset = 4.dp
private val NavigationIconSize = 24.dp
private val NavigationLabelSize = 12.sp
private val NavigationIconLabelGap = 2.dp
private val NavigationBadgeHeight = 17.dp

// ---------------------------------------------------------
// Public tab model
// ---------------------------------------------------------

data class FloatingNavigationItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0,
    val contentDescription: String = label,
)

// ---------------------------------------------------------
// Floating bottom navigation
// ---------------------------------------------------------

@Composable
fun FloatingBottomNavigation(
    items: List<FloatingNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerWidth: Dp = NavigationPreferredWidth,
    containerColor: Color = NavigationSurfaceColor,
    selectedContainerColor: Color = NavigationSelectedColor,
    selectedContentColor: Color = NavigationActiveContent,
    unselectedContentColor: Color = NavigationInactiveContent,
    badgeColor: Color = NavigationBadgeColor,
    badgeContentColor: Color = NavigationBadgeContent,
) {
    if (items.isEmpty()) return

    val safeSelectedIndex = selectedIndex.coerceIn(0, items.lastIndex)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .width(containerWidth)
                .height(NavigationHeight)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.12f),
                    spotColor = Color.Black.copy(alpha = 0.16f),
                ),
            shape = CircleShape,
            color = containerColor,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(NavigationInset),
            ) {
                val slotWidth = maxWidth / items.size

                val animatedPillOffset by animateDpAsState(
                    targetValue = slotWidth * safeSelectedIndex,
                    animationSpec = tween(
                        durationMillis = 280,
                        easing = FastOutSlowInEasing,
                    ),
                    label = "navigationPillPosition",
                )

                // Animated selected pill background
                Box(
                    modifier = Modifier
                        .offset(x = animatedPillOffset)
                        .width(slotWidth)
                        .height(50.dp)
                        .clip(CircleShape)
                        .background(selectedContainerColor),
                )

                // Navigation destinations
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = index == safeSelectedIndex

                        NavigationTab(
                            item = item,
                            selected = isSelected,
                            selectedContentColor = selectedContentColor,
                            unselectedContentColor = unselectedContentColor,
                            badgeColor = badgeColor,
                            badgeContentColor = badgeContentColor,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            onClick = { onItemSelected(index) },
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// Individual tab
// ---------------------------------------------------------

@Composable
private fun NavigationTab(
    item: FloatingNavigationItem,
    selected: Boolean,
    selectedContentColor: Color,
    unselectedContentColor: Color,
    badgeColor: Color,
    badgeContentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val contentColor = if (selected) selectedContentColor else unselectedContentColor
    val interactionSource = remember { MutableInteractionSource() }

    // NOTE: Removed .clip(CircleShape) here so the badge is never clipped by the tab/pill
    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .semantics {
                this.selected = selected
                contentDescription = buildString {
                    append(item.contentDescription)
                    if (item.badgeCount > 0) {
                        append(", ${item.badgeCount} unread")
                    }
                }
            }
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(NavigationIconSize),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = null,
                modifier = Modifier.size(NavigationIconSize),
                tint = contentColor,
            )

            if (item.badgeCount > 0) {
                NavigationCountBadge(
                    count = item.badgeCount,
                    backgroundColor = badgeColor,
                    contentColor = badgeContentColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-3).dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(NavigationIconLabelGap))

        Text(
            text = item.label,
            color = contentColor,
            fontSize = NavigationLabelSize,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ---------------------------------------------------------
// Unread-count badge
// ---------------------------------------------------------

@Composable
private fun NavigationCountBadge(
    count: Int,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    if (count <= 0) return

    val displayCount = if (count > 9999) "9999+" else count.toString()

    Surface(
        modifier = modifier.defaultMinSize(
            minWidth = NavigationBadgeHeight,
            minHeight = NavigationBadgeHeight,
        ),
        shape = CircleShape,
        color = backgroundColor,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .height(NavigationBadgeHeight)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = displayCount,
                color = contentColor,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

// ---------------------------------------------------------
// Ready-to-use Conversations + Contacts
// ---------------------------------------------------------

@Composable
fun MessagesFloatingNavigation(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    unreadConversations: Int,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        FloatingNavigationItem(
            label = "Conversations",
            selectedIcon = Icons.Filled.Sms,
            unselectedIcon = Icons.Outlined.Sms,
            badgeCount = unreadConversations,
            contentDescription = "Conversations",
        ),
        FloatingNavigationItem(
            label = "Contacts",
            selectedIcon = Icons.Outlined.PersonOutline,
            unselectedIcon = Icons.Outlined.PersonOutline,
            contentDescription = "Contacts",
        ),
    )

    FloatingBottomNavigation(
        items = items,
        selectedIndex = selectedIndex,
        onItemSelected = onItemSelected,
        modifier = modifier,
    )
}
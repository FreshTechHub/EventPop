package com.android.example.eventpop.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.android.example.eventpop.ui.theme.AppBarNavy
import com.android.example.eventpop.ui.theme.OrangeAccent

private data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val onClick: () -> Unit,
    val badgeCount: Int = 0
)

@Composable
fun EventPopBottomBar(
    navController: NavController,
    onNavEvents: () -> Unit,
    onNavMap: () -> Unit,
    onNavDiscover: () -> Unit,
    onNavFavorites: () -> Unit,
    onNavProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val items = remember(onNavEvents, onNavMap, onNavDiscover, onNavFavorites, onNavProfile) {
        listOf(
            BottomNavItem(
                route = EventPopDestinations.EVENTS,
                label = "Events",
                selectedIcon = Icons.Filled.Event,
                unselectedIcon = Icons.Outlined.Event,
                onClick = onNavEvents
            ),
            BottomNavItem(
                route = EventPopDestinations.MAP,
                label = "Map",
                selectedIcon = Icons.Filled.Map,
                unselectedIcon = Icons.Outlined.Map,
                onClick = onNavMap
            ),
            BottomNavItem(
                route = EventPopDestinations.DISCOVER,
                label = "Discover",
                selectedIcon = Icons.Filled.Explore,
                unselectedIcon = Icons.Outlined.Explore,
                onClick = onNavDiscover
            ),
            BottomNavItem(
                route = EventPopDestinations.FAVOURITES,
                label = "Saved",
                selectedIcon = Icons.Filled.Bookmark,
                unselectedIcon = Icons.Outlined.BookmarkBorder,
                onClick = onNavFavorites,
                badgeCount = 0
            ),
            BottomNavItem(
                route = EventPopDestinations.PROFILE,
                label = "Profile",
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                onClick = onNavProfile
            )
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppBarNavy,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                OrangeAccent.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    BottomNavItemView(
                        item = item,
                        isSelected = currentRoute == item.route,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItemView(
    item: BottomNavItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val indicatorWidth by animateDpAsState(
        targetValue = if (isSelected) 48.dp else 0.dp,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "bottomIndicatorWidth"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "bottomIconScale"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) {
            OrangeAccent
        } else {
            Color.White.copy(alpha = 0.55f)
        },
        animationSpec = tween(200),
        label = "bottomLabelColor"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) {
            OrangeAccent
        } else {
            Color.White.copy(alpha = 0.55f)
        },
        animationSpec = tween(200),
        label = "bottomIconColor"
    )
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interaction,
                indication = null
            ) { item.onClick() },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(tween(200)) + expandHorizontally(
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ),
            exit = fadeOut(tween(150)) + shrinkHorizontally()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(2.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        color = OrangeAccent,
                        shape = RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp)
                    )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .width(indicatorWidth)
                        .height(32.dp)
                        .background(
                            color = OrangeAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                )
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = iconColor,
                        modifier = Modifier
                            .size(22.dp)
                            .scale(iconScale)
                    )
                    if (item.badgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .offset(x = 6.dp, y = (-4).dp)
                                .size(if (item.badgeCount > 9) 16.dp else 8.dp)
                                .background(
                                    color = Color(0xFFE53935),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.badgeCount > 9) {
                                Text(
                                    text = "9+",
                                    fontSize = 8.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Text(
                text = item.label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = labelColor,
                letterSpacing = 0.2.sp,
                maxLines = 1,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

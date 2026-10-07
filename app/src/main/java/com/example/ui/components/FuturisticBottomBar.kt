package com.example.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainTab

@Composable
fun FuturisticBottomBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onLogClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Colour-changing infinite animation for the center FAB circle
    val infiniteTransition = rememberInfiniteTransition(label = "ColorChangingCircle")
    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF10B981), // Emerald
        targetValue = Color(0xFF06B6D4), // Cyan
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Color1"
    )
    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFF3B82F6), // Blue
        targetValue = Color(0xFF8B5CF6), // Purple
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Color2"
    )
    val color3 by infiniteTransition.animateColor(
        initialValue = Color(0xFFF59E0B), // Amber
        targetValue = Color(0xFFEC4899), // Pink
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Color3"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating Dock Surface
        Surface(
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .testTag("futuristic_bottom_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Expenses Tracker (Upward trend with "grow up" bounce animation)
                FuturisticNavItem(
                    title = "Expenses",
                    icon = Icons.Default.TrendingUp,
                    tab = MainTab.EXPENSES,
                    isSelected = currentTab == MainTab.EXPENSES,
                    onClick = { onTabSelected(MainTab.EXPENSES) },
                    modifier = Modifier.weight(1f).testTag("nav_tab_expenses")
                )

                // 2. Sell's Diary (Moving / tilting animation)
                FuturisticNavItem(
                    title = "Sell's",
                    icon = Icons.Default.PointOfSale,
                    tab = MainTab.SELLS,
                    isSelected = currentTab == MainTab.SELLS,
                    onClick = { onTabSelected(MainTab.SELLS) },
                    modifier = Modifier.weight(1f).testTag("nav_tab_sells")
                )

                // Placeholder Spacer for middle Plus Button
                Spacer(modifier = Modifier.weight(1f))

                // 4. Budget (Fill with coins / pulse animation)
                FuturisticNavItem(
                    title = "Budget",
                    icon = Icons.Default.Savings,
                    tab = MainTab.BUDGET,
                    isSelected = currentTab == MainTab.BUDGET,
                    onClick = { onTabSelected(MainTab.BUDGET) },
                    modifier = Modifier.weight(1f).testTag("nav_tab_budget")
                )

                // 5. Profile (Morphs to profile photo / avatar pulse animation)
                FuturisticNavItem(
                    title = "Profile",
                    icon = Icons.Default.Person,
                    tab = MainTab.PROFILE,
                    isSelected = currentTab == MainTab.PROFILE,
                    onClick = { onTabSelected(MainTab.PROFILE) },
                    modifier = Modifier.weight(1f).testTag("nav_tab_profile")
                )
            }
        }

        // 3. Log Transaction with style of plus in the colour changing circle in the middle-bottom
        Box(
            modifier = Modifier
                .offset(y = (-18).dp)
                .size(62.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        listOf(color1, color2, color3, color1)
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onLogClick
                )
                .testTag("futuristic_log_transaction_btn"),
            contentAlignment = Alignment.Center
        ) {
            // Inner Core Circle
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(color1.copy(alpha = 0.85f), color2.copy(alpha = 0.85f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Log Transaction",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun FuturisticNavItem(
    title: String,
    icon: ImageVector,
    tab: MainTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamic icon animation depending on tab:
    // Expenses: grew up (scale up with bounce)
    // Sells: moving / horizontal tilt
    // Budget: fill with coins pulse
    // Profile: profile photo avatar scale
    val animScale by animateFloatAsState(
        targetValue = when (tab) {
            MainTab.EXPENSES -> if (isSelected) 1.25f else 1.0f
            MainTab.BUDGET -> if (isSelected) 1.20f else 1.0f
            MainTab.PROFILE -> if (isSelected) 1.20f else 1.0f
            MainTab.SELLS -> if (isSelected) 1.15f else 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "TabIconScale"
    )

    val rotationAngle by animateFloatAsState(
        targetValue = if (tab == MainTab.SELLS && isSelected) 14f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "TabRotation"
    )

    val verticalOffset by animateFloatAsState(
        targetValue = if (isSelected) -3f else 0f,
        animationSpec = tween(300),
        label = "TabVerticalOffset"
    )

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .offset(y = verticalOffset.dp)
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            // Specific icon rendering for extra flair:
            when {
                tab == MainTab.BUDGET && isSelected -> {
                    // Budget "fill with coins" effect
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = title,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier
                            .size(20.dp)
                            .scale(animScale)
                    )
                }
                tab == MainTab.PROFILE && isSelected -> {
                    // Profile morph to profile photo avatar
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(22.dp)
                            .scale(animScale)
                    )
                }
                else -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .size(20.dp)
                            .scale(animScale)
                            .rotate(rotationAngle)
                    )
                }
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    }
}

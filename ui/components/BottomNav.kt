// com/achievemeaalk/freedjf/ui/components/BottomNav.kt
package com.achievemeaalk.freedjf.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.achievemeaalk.freedjf.R
import com.achievemeaalk.freedjf.ui.theme.Dimensions
import com.achievemeaalk.freedjf.ui.theme.OnSurface70
import com.achievemeaalk.freedjf.ui.theme.OnSurface80
import com.achievemeaalk.freedjf.ui.theme.headlineSmallBold
import com.achievemeaalk.freedjf.ui.theme.labelSmallSemiBold
import com.canopas.lib.showcase.IntroShowcaseScope

sealed class BottomNavItem(
    val route: String,
    @StringRes val title: Int,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Dashboard : BottomNavItem("dashboard", R.string.dashboard, Icons.Outlined.Dashboard, Icons.Filled.Dashboard)
    object Accounts : BottomNavItem("accounts", R.string.account, Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet)
    object Budgets : BottomNavItem("budgets", R.string.budgets, Icons.Outlined.Bookmarks, Icons.Filled.Bookmarks)
    object Categories : BottomNavItem("categories", R.string.categories, Icons.Outlined.Category, Icons.Filled.Category)
}

private class CurvedShape(
    private val fabRadius: Dp,
    private val cornerRadius: Dp = Dimensions.cornerRadiusLarge
) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): androidx.compose.ui.graphics.Outline {
        val fabRadiusPx = with(density) { fabRadius.toPx() }
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }

        val path = androidx.compose.ui.graphics.Path().apply {
            reset()
            moveTo(0f + cornerRadiusPx, 0f)
            lineTo(size.width / 2 - fabRadiusPx * 1.3f, 0f)

            cubicTo(
                x1 = size.width / 2 - fabRadiusPx * 0.8f, y1 = 0f,
                x2 = size.width / 2 - fabRadiusPx * 0.7f, y2 = fabRadiusPx * 0.9f,
                x3 = size.width / 2, y3 = fabRadiusPx * 0.9f
            )

            cubicTo(
                x1 = size.width / 2 + fabRadiusPx * 0.7f, y1 = fabRadiusPx * 0.9f,
                x2 = size.width / 2 + fabRadiusPx * 0.8f, y2 = 0f,
                x3 = size.width / 2 + fabRadiusPx * 1.3f, y3 = 0f
            )

            lineTo(size.width - cornerRadiusPx, 0f)
            quadraticBezierTo(size.width, 0f, size.width, cornerRadiusPx)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            quadraticBezierTo(0f, size.height, 0f, size.height - cornerRadiusPx)
            lineTo(0f, cornerRadiusPx)
            quadraticBezierTo(0f, 0f, cornerRadiusPx, 0f)
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

@Composable
fun IntroShowcaseScope.CustomBottomNavigationBar(
    modifier: Modifier = Modifier,
    navController: NavController,
    items: List<BottomNavItem>,
    onFabClick: () -> Unit,
    barHeight: Dp = Dimensions.bottomNavigationHeight,
    fabSize: Dp = Dimensions.fabSize,
    // --- THIS IS THE FIX ---
    completedShowcaseRoutes: Set<String>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val fabRadius = fabSize / 2
    val curvedShape = remember(fabRadius) { CurvedShape(fabRadius = fabRadius) }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight + fabRadius / 2)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(barHeight)
                    .background(MaterialTheme.colorScheme.surface, shape = curvedShape)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(barHeight),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.take(items.size / 2).forEach { screen ->
                    BottomBarItem(
                        screen = screen,
                        isSelected = currentDestination?.hierarchy?.any { destination ->
                            val route = destination.route ?: return@any false
                            route == screen.route || route.startsWith(screen.route + "?")
                        } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.width(fabSize + Dimensions.spacingLarge))

                items.takeLast(items.size / 2).forEach { screen ->
                    BottomBarItem(
                        screen = screen,
                        isSelected = currentDestination?.hierarchy?.any { destination ->
                            val route = destination.route ?: return@any false
                            route == screen.route || route.startsWith(screen.route + "?")
                        } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }

            val fabModifier = Modifier
                .size(fabSize)
                .align(Alignment.TopCenter)
                .offset(y = (barHeight - fabSize) / 2 - fabRadius / 3)

            FloatingActionButton(
                onClick = onFabClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                // --- THIS IS THE FIX ---
                modifier = if (!completedShowcaseRoutes.contains("dashboard")) {
                    fabModifier.introShowCaseTarget(
                        index = 0,
                        content = {
                            Column {
                                Text(
                                    text = stringResource(R.string.add_transaction),
                                    style = MaterialTheme.typography.headlineSmallBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = stringResource(R.string.add_transaction_description),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    )
                } else {
                    fabModifier
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_record_button)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

@Composable
fun RowScope.BottomBarItem(
    screen: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    val unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else unselectedIconColor
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary else unselectedTextColor

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow), label = "icon scale"
    )
    val itemPaddingBottom by animateDpAsState(
        targetValue = if (isSelected) Dimensions.spacingExtraSmall else Dimensions.elevationNone,
        animationSpec = spring(stiffness = Spring.StiffnessLow), label = "item padding"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            )
            .padding(bottom = itemPaddingBottom),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = Dimensions.spacingExtraSmall, bottom = Dimensions.spacingExtraSmall)
        ) {
            Icon(
                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                contentDescription = stringResource(id = screen.title),
                tint = iconColor,
                modifier = Modifier
                    .size(Dimensions.iconSizeMedium)
                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
            )
            Spacer(modifier = Modifier.height(Dimensions.spacingExtraSmall))
            Text(
                text = stringResource(id = screen.title),
                style = if (isSelected) MaterialTheme.typography.labelSmallSemiBold else MaterialTheme.typography.labelSmall,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
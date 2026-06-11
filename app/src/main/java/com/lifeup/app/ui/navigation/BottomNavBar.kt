package com.lifeup.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: @Composable () -> Unit,
    val unselectedIcon: @Composable () -> Unit
)

private val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Today.route,
        label = "今日",
        selectedIcon = { Icon(Icons.Filled.WbSunny, contentDescription = "今日", modifier = Modifier.size(24.dp)) },
        unselectedIcon = { Icon(Icons.Outlined.WbSunny, contentDescription = "今日", modifier = Modifier.size(24.dp)) }
    ),
    BottomNavItem(
        route = Screen.Skills.route,
        label = "技能",
        selectedIcon = { Icon(Icons.Filled.AutoAwesome, contentDescription = "技能", modifier = Modifier.size(24.dp)) },
        unselectedIcon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = "技能", modifier = Modifier.size(24.dp)) }
    ),
    BottomNavItem(
        route = Screen.Character.route,
        label = "角色馆",
        selectedIcon = { Icon(Icons.Filled.Person, contentDescription = "角色馆", modifier = Modifier.size(24.dp)) },
        unselectedIcon = { Icon(Icons.Outlined.Person, contentDescription = "角色馆", modifier = Modifier.size(24.dp)) }
    ),
    BottomNavItem(
        route = Screen.Profile.route,
        label = "我的",
        selectedIcon = { Icon(Icons.Filled.Settings, contentDescription = "我的", modifier = Modifier.size(24.dp)) },
        unselectedIcon = { Icon(Icons.Outlined.Settings, contentDescription = "我的", modifier = Modifier.size(24.dp)) }
    )
)

@Composable
fun LifeUpBottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        tonalElevation = 2.dp
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    if (isSelected) item.selectedIcon() else item.unselectedIcon()
                },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}

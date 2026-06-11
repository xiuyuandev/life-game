package com.lifeup.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)

private val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Today.route,
        label = "今日",
        icon = { Icon(Icons.Default.WbSunny, contentDescription = "今日") }
    ),
    BottomNavItem(
        route = Screen.Skills.route,
        label = "技能",
        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "技能") }
    ),
    BottomNavItem(
        route = Screen.Character.route,
        label = "角色馆",
        icon = { Icon(Icons.Default.Person, contentDescription = "角色馆") }
    ),
    BottomNavItem(
        route = Screen.Profile.route,
        label = "我的",
        icon = { Icon(Icons.Default.Settings, contentDescription = "我的") }
    )
)

@Composable
fun LifeUpBottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = item.icon,
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}

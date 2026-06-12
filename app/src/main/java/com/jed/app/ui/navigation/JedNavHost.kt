package com.jed.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jed.app.ui.components.ConnectionStatusBar
import com.jed.app.ui.dashboard.DashboardScreen
import com.jed.app.ui.diag.DiagScreen
import com.jed.app.ui.home.HomeScreen
import com.jed.app.ui.keys.KeysScreen
import com.jed.app.ui.service.ServiceScreen
import com.jed.app.ui.theme.Charcoal
import com.jed.app.ui.theme.DarkGray
import com.jed.app.ui.theme.LightGray
import com.jed.app.ui.theme.OrangeAccent

enum class JedScreen(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Home", Icons.Default.Home),
    Dashboard("dashboard", "Dashboard", Icons.Default.Dashboard),
    Diag("diag", "Diag", Icons.Default.Warning),
    Service("service", "Service", Icons.Default.Settings),
    Keys("keys", "Keys", Icons.Default.Key)
}

@Composable
fun JedNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        topBar = { ConnectionStatusBar() },
        bottomBar = {
            NavigationBar(containerColor = DarkGray) {
                JedScreen.entries.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OrangeAccent,
                            selectedTextColor = OrangeAccent,
                            unselectedIconColor = LightGray,
                            unselectedTextColor = LightGray,
                            indicatorColor = Charcoal
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = JedScreen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(JedScreen.Home.route) {
                HomeScreen(onNavigateToDashboard = {
                    navController.navigate(JedScreen.Dashboard.route)
                })
            }
            composable(JedScreen.Dashboard.route) { DashboardScreen() }
            composable(JedScreen.Diag.route) { DiagScreen() }
            composable(JedScreen.Service.route) { ServiceScreen() }
            composable(JedScreen.Keys.route) { KeysScreen() }
        }
    }
}

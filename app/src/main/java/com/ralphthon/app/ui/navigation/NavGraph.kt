package com.ralphthon.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object CustomerList : Screen("customers")
    object CardNewsList : Screen("customers/{customerId}/cards") {
        fun createRoute(customerId: Long) = "customers/$customerId/cards"
    }
    object CardDetail : Screen("cards/{cardId}") {
        fun createRoute(cardId: Long) = "cards/$cardId"
    }
    object Search : Screen("search")
    object Upload : Screen("upload")
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("고객", Icons.Default.Person, Screen.CustomerList.route),
    BottomNavItem("검색", Icons.Default.Search, Screen.Search.route),
    BottomNavItem("업로드", Icons.Default.Add, Screen.Upload.route)
)

@Composable
fun ZipiNavGraph(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.CustomerList.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.CustomerList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.CustomerList.route) {
                PlaceholderScreen("고객 목록")
            }
            composable(
                Screen.CardNewsList.route,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) {
                PlaceholderScreen("카드뉴스 목록")
            }
            composable(
                Screen.CardDetail.route,
                arguments = listOf(navArgument("cardId") { type = NavType.LongType })
            ) {
                PlaceholderScreen("카드 상세")
            }
            composable(Screen.Search.route) {
                PlaceholderScreen("검색")
            }
            composable(Screen.Upload.route) {
                PlaceholderScreen("업로드")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
    }
}

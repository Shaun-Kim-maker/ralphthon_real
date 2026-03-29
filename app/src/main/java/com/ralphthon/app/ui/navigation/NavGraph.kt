package com.ralphthon.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ralphthon.app.ui.card.CardDetailScreen
import com.ralphthon.app.ui.card.CardNewsListScreen
import com.ralphthon.app.ui.customer.CustomerBriefBottomSheet
import com.ralphthon.app.ui.customer.CustomerListScreen
import com.ralphthon.app.ui.search.SearchScreen
import com.ralphthon.app.ui.upload.UploadScreen
import androidx.compose.material3.MaterialTheme

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZipiNavGraph(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    var showBriefSheet by remember { mutableStateOf(false) }
    var briefCustomerId by remember { mutableStateOf(0L) }

    Scaffold(
        topBar = {
            if (showBottomBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Zipi",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
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
                CustomerListScreen(
                    onCustomerClick = { customerId ->
                        briefCustomerId = customerId
                        showBriefSheet = true
                    }
                )
            }
            composable(
                Screen.CardNewsList.route,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) {
                CardNewsListScreen(
                    onCardClick = { cardId ->
                        navController.navigate(Screen.CardDetail.createRoute(cardId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                Screen.CardDetail.route,
                arguments = listOf(navArgument("cardId") { type = NavType.LongType })
            ) {
                CardDetailScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onResultClick = { sourceId ->
                        navController.navigate(Screen.CardDetail.createRoute(sourceId))
                    }
                )
            }
            composable(Screen.Upload.route) {
                UploadScreen()
            }
        }

        if (showBriefSheet) {
            CustomerBriefBottomSheet(
                customerId = briefCustomerId,
                onDismiss = { showBriefSheet = false },
                onNavigateToDetail = {
                    showBriefSheet = false
                    navController.navigate(Screen.CardNewsList.createRoute(briefCustomerId))
                }
            )
        }
    }
}

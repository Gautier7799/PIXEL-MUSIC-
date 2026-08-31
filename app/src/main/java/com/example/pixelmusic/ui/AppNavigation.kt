package com.example.pixelmusic.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main_screen") {
        
        composable("main_screen") {
            MainScreen(
                onNavigateToSearch = { navController.navigate("search_screen") },
                onNavigateToSettings = { navController.navigate("settings_screen") }
            )
        }

        composable("search_screen") {
            // سنضع شاشة البحث هنا لاحقاً
        }

        composable("settings_screen") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

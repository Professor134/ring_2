package com.example.ringapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.ringapp.ui.AppViewModel
import com.example.ringapp.ui.home.HomeScreen
import com.example.ringapp.ui.onboarding.OnboardingScreen

@Composable
fun RingNavigation(viewModel: AppViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = if (state.onboardingComplete) "home" else "onboarding") {
        composable("onboarding") {
            OnboardingScreen {
                viewModel.completeOnboarding()
                navController.navigate("home") {
                    popUpTo("onboarding") { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        composable("home") { HomeScreen() }
        composable(
            route = "habit/{habitId}",
            arguments = listOf(navArgument("habitId") { type = NavType.LongType }),
            deepLinks = listOf(navDeepLink { uriPattern = "https://myapp.example/habit/{habitId}" })
        ) { HomeScreen() }
        composable(
            route = "task/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType }),
            deepLinks = listOf(navDeepLink { uriPattern = "https://myapp.example/task/{taskId}" })
        ) { HomeScreen() }
        composable(
            route = "addHabit",
            deepLinks = listOf(navDeepLink { uriPattern = "ring://addHabit" })
        ) { HomeScreen() }
        composable(
            route = "addTask",
            deepLinks = listOf(navDeepLink { uriPattern = "ring://addTask" })
        ) { HomeScreen() }
        composable("profile") { HomeScreen() }
    }
}

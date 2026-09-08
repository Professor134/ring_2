package com.example.ringapp.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import com.example.ringapp.ui.AppViewModel
import com.example.ringapp.ui.home.HomeScreen
import com.example.ringapp.ui.onboarding.OnboardingScreen
import com.example.ringapp.ui.habits.HabitDetailScreen
import com.example.ringapp.ui.habits.HabitListScreen
import com.example.ringapp.ui.habits.AddHabitScreen
import com.example.ringapp.ui.tasks.TasksScreen
import com.example.ringapp.ui.tasks.AddTaskScreen
import com.example.ringapp.ui.analytics.MainAnalyticsScreen
import com.example.ringapp.ui.profile.ProfileScreen
import com.example.ringapp.ui.profile.EditProfileScreen
import com.example.ringapp.ui.profile.AppearanceScreen
import com.example.ringapp.ui.profile.PointHistoryScreen
import com.example.ringapp.ui.profile.ProfileInfoScreen
import com.example.ringapp.ui.analytics.PersonalAnalyticsScreen

@Composable
fun AppNavGraph(viewModel: AppViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NavigationDestination.HOME.route) {
        composable(Screen.Splash) {
            SplashRoute(state.initializationState) {
                navController.navigate(if (state.onboardingComplete) NavigationDestination.HOME.route else Screen.Onboarding) {
                    popUpTo(Screen.Splash) { inclusive = true }
                }
            }
        }
        composable(Screen.Onboarding) {
            OnboardingScreen {
                viewModel.completeOnboarding()
                navController.navigate(NavigationDestination.HOME.route) {
                    popUpTo(Screen.Onboarding) { inclusive = true }
                }
            }
        }
        composable(Screen.HabitDetail, arguments = listOf(navArgument("habitId") { type = NavType.StringType })) {
            PersonalAnalyticsScreen(onBack = { navController.popBackStack() }, onEdit = { navController.navigate(Screen.editHabit(it)) })
        }
        composable(Screen.HabitAnalytics, arguments = listOf(navArgument("habitId") { type = NavType.StringType })) {
            PersonalAnalyticsScreen(onBack = { navController.popBackStack() }, onEdit = { navController.navigate(Screen.editHabit(it)) })
        }
        composable(Screen.AddHabit) {
            AddHabitScreen(onSaved = { navController.popBackStack() }, onBack = { navController.popBackStack() })
        }
        composable(Screen.EditHabit, arguments = listOf(navArgument("habitId") { type = NavType.LongType })) {
            AddHabitScreen(habitId = it.arguments?.getLong("habitId"), onSaved = { navController.popBackStack() }, onBack = { navController.popBackStack() })
        }
        composable(Screen.AddTask) { AddTaskScreen(onSaved = { navController.popBackStack() }, onBack = { navController.popBackStack() }) }
        composable(Screen.EditTask, arguments = listOf(navArgument("taskId") { type = NavType.LongType })) { entry -> AddTaskScreen(taskId = entry.arguments?.getLong("taskId"), onSaved = { navController.popBackStack() }, onBack = { navController.popBackStack() }) }
        composable(Screen.EditProfile) { EditProfileScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.Appearance) { AppearanceScreen(viewModel) }
        composable(Screen.PointHistory) { PointHistoryScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.Notifications) { ProfileInfoScreen("Notifications", "Task reminders and local notifications are managed on this device.") { navController.popBackStack() } }
        composable(Screen.Backup) { ProfileInfoScreen("Backup & Restore", "Use the local database backup tools to export or restore RING data.") { navController.popBackStack() } }
        composable(Screen.PersonalAnalytics) { PersonalAnalyticsScreen() }
        NavigationDestination.entries.forEach { destination ->
            composable(destination.route) {
                AdaptiveNavigationScaffold(navController, destination) {
                    when (destination) {
                        NavigationDestination.HABITS -> HabitListScreen(
                            onHabitClick = { navController.navigate(Screen.habitAnalytics(it)) },
                            onAddHabit = { navController.navigate(Screen.AddHabit) }
                        )
                        NavigationDestination.TASKS -> TasksScreen(onAddTask = { navController.navigate(Screen.AddTask) }, onTaskClick = { navController.navigate(Screen.editTask(it)) })
                        NavigationDestination.INSIGHTS -> MainAnalyticsScreen(onPersonal = { navController.navigate(Screen.PersonalAnalytics) })
                        NavigationDestination.PROFILE -> ProfileScreen(viewModel, onEdit = { navController.navigate(Screen.EditProfile) }, onAppearance = { navController.navigate(Screen.Appearance) }, onPointHistory = { navController.navigate(Screen.PointHistory) }, onNotifications = { navController.navigate(Screen.Notifications) }, onBackup = { navController.navigate(Screen.Backup) })
                        else -> HomeScreen(
                            onHabitClick = { navController.navigate(Screen.habitAnalytics(it)) },
                            onAddHabit = { navController.navigate(Screen.AddHabit) },
                            onAddTask = { navController.navigate(Screen.AddTask) },
                            onSeeAllHabits = { navController.navigate(NavigationDestination.HABITS.route) },
                            onSeeAllTasks = { navController.navigate(NavigationDestination.TASKS.route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashRoute(initializationState: String, onReady: () -> Unit) {
    LaunchedEffect(initializationState) {
        if (initializationState == "COMPLETE") onReady()
    }
}

@Composable
private fun AdaptiveNavigationScaffold(
    navController: NavHostController,
    selectedDestination: NavigationDestination,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isCompact = this.maxWidth < 600.dp
        if (isCompact) {
            Scaffold(
                bottomBar = {
                    NavigationBar(Modifier.semantics { contentDescription = "Main navigation" }) {
                        NavigationDestination.entries.forEach { destination ->
                            NavigationBarItem(
                                selected = destination == selectedDestination,
                                onClick = { navController.navigateTo(destination) },
                                icon = { NavigationMarker(destination) },
                                label = { androidx.compose.material3.Text(destination.label) }
                            )
                        }
                    }
                }
            ) { padding ->
                androidx.compose.foundation.layout.Box(Modifier.padding(padding)) { content() }
            }
        } else {
            androidx.compose.foundation.layout.Row(Modifier.fillMaxSize().semantics { contentDescription = "Main navigation rail" }) {
                NavigationRail {
                    NavigationDestination.entries.forEach { destination ->
                        NavigationRailItem(
                            selected = destination == selectedDestination,
                            onClick = { navController.navigateTo(destination) },
                            icon = { NavigationMarker(destination) },
                            label = { androidx.compose.material3.Text(destination.label) }
                        )
                    }
                }
                androidx.compose.foundation.layout.Box(Modifier.weight(1f)) { content() }
            }
        }
    }
}

private fun NavHostController.navigateTo(destination: NavigationDestination) {
    navigate(destination.route) {
        popUpTo(NavigationDestination.HOME.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
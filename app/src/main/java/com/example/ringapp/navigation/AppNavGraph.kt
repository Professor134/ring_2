// -----------------------------------------------------------------------------
// Project: Ring 2
// File: app\src\main\java\com\example\ringapp\navigation\AppNavGraph.kt
// Purpose: This file contains the application logic and is documented for clarity without changing behavior.
// -----------------------------------------------------------------------------

package com.example.ringapp.navigation

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
import com.example.ringapp.ui.onboarding.RingSplashScreen
import com.example.ringapp.ui.habits.HabitDetailScreen
import com.example.ringapp.ui.habits.HabitListScreen
import com.example.ringapp.ui.habits.AddHabitScreen
import com.example.ringapp.ui.tasks.TasksScreen
import com.example.ringapp.ui.tasks.AddTaskScreen
import com.example.ringapp.ui.tasks.TaskDetailScreen
import com.example.ringapp.ui.analytics.MainAnalyticsScreen
import com.example.ringapp.ui.profile.ProfileScreen
import com.example.ringapp.ui.profile.EditProfileScreen
import com.example.ringapp.ui.profile.AppearanceScreen
import com.example.ringapp.ui.profile.PointHistoryScreen
import com.example.ringapp.ui.profile.ProfileInfoScreen
import com.example.ringapp.ui.analytics.PersonalAnalyticsScreen

@Composable
// Handles the app nav graph logic for this component without modifying runtime behavior.
// Manages the app nav graph flow without changing runtime behavior.
fun AppNavGraph(viewModel: AppViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var onboardingSaving by remember { mutableStateOf(false) }
    var onboardingError by remember { mutableStateOf<String?>(null) }
    val finishSplash: () -> Unit = remember(viewModel) {
        { viewModel.markFirstLaunchSplashSeen(); Unit }
    }

    if (!state.ready || (!state.onboardingComplete && !state.firstLaunchSplashSeen)) {
        RingSplashScreen(
            isLoading = !state.ready,
            onFinished = if (state.ready) finishSplash else null
        )
        return
    }

    if (!state.onboardingComplete) {
        OnboardingScreen(
            isSaving = onboardingSaving,
            errorMessage = onboardingError,
            onComplete = { details ->
                onboardingSaving = true
                onboardingError = null
                viewModel.completeOnboarding(
                    details = details,
                    onComplete = { onboardingSaving = false },
                    onError = { message ->
                        onboardingSaving = false
                        onboardingError = message
                    }
                )
            }
        )
        return
    }

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = NavigationDestination.HOME.route,
        enterTransition = { fadeIn(tween(350)) + slideInHorizontally(tween(400)) { it / 14 } },
        exitTransition = { fadeOut(tween(250)) + slideOutHorizontally(tween(300)) { -it / 24 } },
        popEnterTransition = { fadeIn(tween(350)) + slideInHorizontally(tween(400)) { -it / 14 } },
        popExitTransition = { fadeOut(tween(250)) + slideOutHorizontally(tween(300)) { it / 24 } }
    ) {
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
        composable(Screen.TaskDetail, arguments = listOf(navArgument("taskId") { type = NavType.LongType })) {
            TaskDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Screen.editTask(it)) }
            )
        }
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
                        NavigationDestination.TASKS -> TasksScreen(
                            onAddTask = { navController.navigate(Screen.AddTask) },
                            onTaskClick = { navController.navigate(Screen.taskDetail(it)) }
                        )
                        NavigationDestination.INSIGHTS -> MainAnalyticsScreen(onPersonal = { navController.navigate(Screen.PersonalAnalytics) })
                        NavigationDestination.PROFILE -> ProfileScreen(viewModel, onEdit = { navController.navigate(Screen.EditProfile) }, onAppearance = { navController.navigate(Screen.Appearance) }, onPointHistory = { navController.navigate(Screen.PointHistory) }, onNotifications = { navController.navigate(Screen.Notifications) }, onBackup = { navController.navigate(Screen.Backup) })
                        else -> HomeScreen(
                            onHabitClick = { navController.navigate(Screen.habitAnalytics(it)) },
                            onAddHabit = { navController.navigate(Screen.AddHabit) },
                            onAddTask = { navController.navigate(Screen.AddTask) },
                            onTaskClick = { navController.navigate(Screen.taskDetail(it)) },
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
// Handles the adaptive navigation scaffold logic for this component without modifying runtime behavior.
// Manages the adaptive navigation scaffold flow without changing runtime behavior.
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
                    NavigationBar(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .border(
                                BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                                RoundedCornerShape(22.dp)
                            )
                            .semantics { contentDescription = "Main navigation" },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.14f),
                        tonalElevation = 0.dp,
                        windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
                    ) {
                        NavigationDestination.entries.forEach { destination ->
                            NavigationBarItem(
                                selected = destination == selectedDestination,
                                onClick = { navController.navigateTo(destination) },
                                modifier = Modifier.animateContentSize(),
                                icon = { NavigationMarker(destination) },
                                label = {
                                    androidx.compose.material3.Text(
                                        text = destination.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                },
                                alwaysShowLabel = true,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                )
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

// Handles the nav host controller logic for this component without modifying runtime behavior.
// Manages the nav host controller flow without changing runtime behavior.
private fun NavHostController.navigateTo(destination: NavigationDestination) {
    navigate(destination.route) {
        popUpTo(NavigationDestination.HOME.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

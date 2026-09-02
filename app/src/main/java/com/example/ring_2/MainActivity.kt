package com.example.ring_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ring_2.data.AppDatabase
import com.example.ring_2.data.MainRepository
import com.example.ring_2.logic.RingNotificationManager
import com.example.ring_2.ui.MainViewModel
import com.example.ring_2.ui.navigation.Screen
import com.example.ring_2.ui.screens.*
import com.example.ring_2.ui.theme.RingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        try {
            RingNotificationManager.createNotificationChannel(this)
            com.example.ring_2.logic.MotivationManager.scheduleDailyMotivation(this)
            
            val database = AppDatabase.getDatabase(this)
            val repository = MainRepository(
                applicationContext,
                database.habitDao(), 
                database.taskDao(), 
                database.userDao(), 
                database.categoryDao(),
                database.notificationDao(),
                database.achievementDao()
            )
            val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(repository) as T
                }
            })[MainViewModel::class.java]

            setContent {
                val profile by viewModel.userProfile.collectAsState()
                val themePref = profile?.themePreference ?: "System"
                
                RingTheme(
                    darkTheme = when(themePref) {
                        "Dark" -> true
                        "Light" -> false
                        else -> androidx.compose.foundation.isSystemInDarkTheme()
                    }
                ) {
                    val navController = rememberNavController()
                    MainScaffold(navController, viewModel)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback content if something critical fails
            setContent {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Something went wrong during startup. Please restart the app.")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(navController: NavHostController, viewModel: MainViewModel) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            if (currentDestination?.route in listOf(Screen.Home.route, Screen.Habits.route, Screen.Tasks.route, Screen.Profile.route)) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    val items = listOf(
                        Screen.Home,
                        Screen.Habits,
                        Screen.Tasks,
                        Screen.Profile
                    )
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(getIconForScreen(screen), contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { 
                HomeScreen(viewModel, 
                    onAddHabit = { navController.navigate("add_habit") },
                    onAddTask = { navController.navigate("add_task") },
                    onHabitClick = { id -> navController.navigate(Screen.HabitDetail.createRoute(id)) },
                    onTaskClick = { id -> navController.navigate("edit_task/$id") },
                    onSeeAllHabits = { navController.navigate(Screen.Habits.route) },
                    onSeeAllTasks = { navController.navigate(Screen.Tasks.route) }
                ) 
            }
            composable(Screen.Habits.route) { 
                HabitsScreen(viewModel, 
                    onHabitClick = { id -> navController.navigate(Screen.HabitDetail.createRoute(id)) },
                    onAddHabit = { navController.navigate("add_habit") }
                ) 
            }
            composable(Screen.Tasks.route) { 
                TasksScreen(viewModel, 
                    onAddTask = { navController.navigate("add_task") },
                    onTaskClick = { id -> navController.navigate("edit_task/$id") }
                ) 
            }
            composable(Screen.Profile.route) { 
                ProfileScreen(
                    viewModel = viewModel,
                    onEditProfile = { navController.navigate("edit_profile") },
                    onPointHistory = { navController.navigate("point_history") },
                    onAppearance = { navController.navigate("appearance") },
                    onNotifications = { navController.navigate("notifications_history") },
                    onBackupRestore = { navController.navigate("data_management") }
                ) 
            }
            composable("appearance") {
                AppearanceScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable("data_management") {
                DataManagementScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable("notifications_history") {
                NotificationsHistoryScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable("edit_profile") {
                EditProfileScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable("point_history") {
                PointHistoryScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.HabitDetail.route) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId")?.toLongOrNull() ?: return@composable
                HabitDetailScreen(
                    viewModel = viewModel,
                    habitId = habitId,
                    onBack = { navController.popBackStack() },
                    onEditHabit = { navController.navigate("edit_habit/$habitId") }
                )
            }
            composable("add_habit") {
                AddHabitScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable("edit_habit/{habitId}") { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId")?.toLongOrNull() ?: return@composable
                AddHabitScreen(viewModel, habitId = habitId, onBack = { navController.popBackStack() })
            }
            composable("add_task") {
                AddTaskScreen(viewModel, onBack = { navController.popBackStack() })
            }
            composable("edit_task/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull() ?: return@composable
                AddTaskScreen(viewModel, taskId = taskId, onBack = { navController.popBackStack() })
            }
        }
    }
}

fun getIconForScreen(screen: Screen) = when (screen) {
    Screen.Home -> Icons.Default.Home
    Screen.Habits -> Icons.AutoMirrored.Filled.List
    Screen.Tasks -> Icons.Default.CheckCircle
    Screen.Profile -> Icons.Default.Person
    else -> Icons.Default.Home
}

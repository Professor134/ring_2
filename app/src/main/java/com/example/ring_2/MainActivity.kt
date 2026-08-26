package com.example.ring_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ring_2.data.AppDatabase
import com.example.ring_2.data.MainRepository
import com.example.ring_2.ui.MainViewModel
import com.example.ring_2.ui.navigation.Screen
import com.example.ring_2.ui.screens.*
import com.example.ring_2.ui.theme.RingTheme

import com.example.ring_2.logic.RingNotificationManager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        RingNotificationManager.createNotificationChannel(this)
        
        val database = AppDatabase.getDatabase(this)
        val repository = MainRepository(database.habitDao(), database.taskDao(), database.userDao())
        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository) as T
            }
        })[MainViewModel::class.java]

        setContent {
            RingTheme {
                val navController = rememberNavController()
                MainScaffold(navController, viewModel)
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
            NavigationBar(
                containerColor = Color(0xFF1E1E1E),
                tonalElevation = 0.dp
            ) {
                val items = listOf(
                    Screen.Home,
                    Screen.Habits,
                    Screen.Tasks,
                    Screen.Insights,
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
                            selectedIconColor = Color(0xFF00E676),
                            selectedTextColor = Color(0xFF00E676),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) { 
                HomeScreen(viewModel, 
                    onAddHabit = { navController.navigate("add_habit") },
                    onAddTask = { /* navigate to add task */ }
                ) 
            }
            composable(Screen.Habits.route) { 
                HabitsScreen(viewModel, 
                    onHabitClick = { id -> navController.navigate(Screen.HabitDetail.createRoute(id)) },
                    onAddHabit = { navController.navigate("add_habit") }
                ) 
            }
            composable(Screen.Tasks.route) { TasksScreen(viewModel, onAddTask = {}) }
            composable(Screen.Insights.route) { InsightsScreen(viewModel) }
            composable(Screen.Profile.route) { ProfileScreen(viewModel) }
            composable(Screen.HabitDetail.route) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getString("habitId")?.toLongOrNull() ?: return@composable
                HabitDetailScreen(viewModel, habitId, onBack = { navController.popBackStack() })
            }
            composable("add_habit") {
                AddHabitScreen(viewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}

fun getIconForScreen(screen: Screen) = when (screen) {
    Screen.Home -> Icons.Default.Home
    Screen.Habits -> Icons.Default.List
    Screen.Tasks -> Icons.Default.CheckCircle
    Screen.Insights -> Icons.Default.Info
    Screen.Profile -> Icons.Default.Person
    else -> Icons.Default.Home
}

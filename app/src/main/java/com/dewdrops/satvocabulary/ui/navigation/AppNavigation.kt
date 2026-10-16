package com.dewdrops.satvocabulary.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dewdrops.satvocabulary.ui.screens.FlashcardScreen
import com.dewdrops.satvocabulary.ui.screens.QuizScreen
import com.dewdrops.satvocabulary.ui.screens.SettingsScreen
import com.dewdrops.satvocabulary.ui.screens.WordListScreen
import com.dewdrops.satvocabulary.ui.viewmodel.VocabularyViewModel

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Flashcards : Screen("flashcards", "Flashcards", Icons.Default.Book)
    object Dictionary : Screen("dictionary", "Dictionary", Icons.AutoMirrored.Filled.List)
    object Quiz : Screen("quiz", "Quiz", Icons.Default.Quiz)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun AppNavigation(viewModel: VocabularyViewModel) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Flashcards,
        Screen.Dictionary,
        Screen.Quiz,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Flashcards.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Flashcards.route) { FlashcardScreen(viewModel) }
            composable(Screen.Dictionary.route) { WordListScreen(viewModel) }
            composable(Screen.Quiz.route) { QuizScreen(viewModel) }
            composable(Screen.Settings.route) { SettingsScreen(viewModel) }
        }
    }
}

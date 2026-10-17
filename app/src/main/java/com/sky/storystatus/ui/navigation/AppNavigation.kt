package com.sky.storystatus.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sky.storystatus.ui.TemplateViewModel
import com.sky.storystatus.ui.editor.EditorScreen
import com.sky.storystatus.ui.editor.PreviewScreen
import com.sky.storystatus.ui.explore.ExploreScreen
import com.sky.storystatus.ui.favorites.FavoritesScreen
import com.sky.storystatus.ui.home.HomeScreen
import kotlinx.coroutines.launch

/**
 * Navigation destinations for the bottom bar.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Explore : Screen("explore", "Explore", Icons.Filled.Explore, Icons.Outlined.Explore)
    data object Favorites : Screen("favorites", "Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
}

private val bottomBarItems = listOf(Screen.Home, Screen.Explore, Screen.Favorites)

@Composable
fun MainApp(viewModel: TemplateViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val templates by viewModel.templates.collectAsState()
    val featuredTemplates by viewModel.featuredTemplates.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val languages by viewModel.languages.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentCustomization by viewModel.currentCustomization.collectAsState()

    val tags by viewModel.tags.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Determine whether to show bottom bar
    val showBottomBar = currentRoute in bottomBarItems.map { it.route }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomBarItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    screen.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(200)) }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    templates = templates,
                    featuredTemplates = featuredTemplates,
                    categories = categories,
                    favorites = favorites,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onTemplateSelected = { templateId ->
                        viewModel.resetCustomization()
                        navController.navigate("preview/$templateId")
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onSearchClick = {
                        navController.navigate(Screen.Explore.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onFavoritesClick = {
                        navController.navigate(Screen.Favorites.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onRetry = { viewModel.loadTemplates() }
                )
            }
            composable(Screen.Explore.route) {
                ExploreScreen(
                    templates = templates,
                    categories = categories,
                    languages = languages,
                    tags = tags,
                    selectedTag = selectedTag,
                    onTagSelected = { viewModel.setSelectedTag(it) },
                    favorites = favorites,
                    onTemplateSelected = { templateId ->
                        viewModel.resetCustomization()
                        navController.navigate("preview/$templateId")
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                )
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    templates = templates,
                    favoriteIds = favorites,
                    onTemplateSelected = { templateId ->
                        viewModel.resetCustomization()
                        navController.navigate("preview/$templateId")
                    },
                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                )
            }
            composable("preview/{templateId}") { backStackEntry ->
                val templateId = backStackEntry.arguments?.getString("templateId")
                val template = templateId?.let { viewModel.getTemplateById(it) }
                val isFavorite = templateId?.let { favorites.contains(it) } ?: false
                PreviewScreen(
                    template = template,
                    isFavorite = isFavorite,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate("editor/$id") },
                    onToggleFavorite = { id -> viewModel.toggleFavorite(id) },
                    onSave = {
                        templateId?.let { id ->
                            coroutineScope.launch {
                                val success = viewModel.saveToGallery(id)
                                snackbarHostState.showSnackbar(
                                    if (success) "✅ Saved to gallery!" else "❌ Failed to save."
                                )
                            }
                        }
                    },
                    onShare = {
                        templateId?.let { id ->
                            coroutineScope.launch {
                                val success = viewModel.shareStatus(id)
                                if (!success) {
                                    snackbarHostState.showSnackbar("❌ Failed to share.")
                                }
                            }
                        }
                    },
                    renderPreview = { id -> viewModel.renderTemplate(id) }
                )
            }
            composable("editor/{templateId}") { backStackEntry ->
                val templateId = backStackEntry.arguments?.getString("templateId") ?: ""
                EditorScreen(
                    templateId = templateId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

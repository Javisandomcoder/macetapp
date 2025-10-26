package com.example.pruebaandroid.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pruebaandroid.data.PlantViewModel
import com.example.pruebaandroid.screens.AddEditPlantScreen
import com.example.pruebaandroid.screens.PlantDetailScreen
import com.example.pruebaandroid.screens.PlantListScreen
import com.example.pruebaandroid.screens.PlantPhotoGalleryScreen
import com.example.pruebaandroid.screens.AddPhotoScreen
import com.example.pruebaandroid.screens.SettingsScreen

sealed class Screen(val route: String) {
    object PlantList : Screen("plant_list")
    object AddPlant : Screen("add_plant")
    object PlantDetail : Screen("plant_detail/{plantId}") {
        fun createRoute(plantId: Int) = "plant_detail/$plantId"
    }
    object EditPlant : Screen("edit_plant/{plantId}") {
        fun createRoute(plantId: Int) = "edit_plant/$plantId"
    }
    object PhotoGallery : Screen("photo_gallery/{plantId}") {
        fun createRoute(plantId: Int) = "photo_gallery/$plantId"
    }
    object AddPhoto : Screen("add_photo/{plantId}") {
        fun createRoute(plantId: Int) = "add_photo/$plantId"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: PlantViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.PlantList.route
    ) {
        composable(Screen.PlantList.route) {
            PlantListScreen(
                viewModel = viewModel,
                onNavigateToAddPlant = {
                    navController.navigate(Screen.AddPlant.route)
                },
                onNavigateToPlantDetail = { plantId ->
                    navController.navigate(Screen.PlantDetail.createRoute(plantId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.AddPlant.route) {
            AddEditPlantScreen(
                viewModel = viewModel,
                plantId = null,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.PlantDetail.route,
            arguments = listOf(navArgument("plantId") { type = NavType.IntType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getInt("plantId")
            plantId?.let {
                PlantDetailScreen(
                    viewModel = viewModel,
                    plantId = it,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEdit = { id ->
                        navController.navigate(Screen.EditPlant.createRoute(id))
                    },
                    onNavigateToGallery = { id ->
                        navController.navigate(Screen.PhotoGallery.createRoute(id))
                    }
                )
            }
        }

        composable(
            route = Screen.PhotoGallery.route,
            arguments = listOf(navArgument("plantId") { type = NavType.IntType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getInt("plantId")
            plantId?.let {
                PlantPhotoGalleryScreen(
                    viewModel = viewModel,
                    plantId = it,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onAddPhoto = {
                        navController.navigate(Screen.AddPhoto.createRoute(it))
                    }
                )
            }
        }

        composable(
            route = Screen.AddPhoto.route,
            arguments = listOf(navArgument("plantId") { type = NavType.IntType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getInt("plantId")
            plantId?.let {
                AddPhotoScreen(
                    viewModel = viewModel,
                    plantId = it,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(
            route = Screen.EditPlant.route,
            arguments = listOf(navArgument("plantId") { type = NavType.IntType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getInt("plantId")
            AddEditPlantScreen(
                viewModel = viewModel,
                plantId = plantId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

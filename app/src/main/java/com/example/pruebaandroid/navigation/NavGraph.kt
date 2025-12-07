package com.example.pruebaandroid.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.pruebaandroid.ui.viewmodels.PlantViewModel
import com.example.pruebaandroid.screens.*
import kotlinx.serialization.Serializable

// Define serializable objects/classes for each screen
@Serializable
object PlantList

@Serializable
object AddPlant

@Serializable
data class PlantDetail(val plantId: Int)

@Serializable
data class EditPlant(val plantId: Int)

@Serializable
data class PhotoGallery(val plantId: Int)

@Serializable
data class AddPhoto(val plantId: Int)

@Serializable
data class CareHistory(val plantId: Int, val plantName: String)

@Serializable
data class AddCareActivity(val plantId: Int, val plantName: String)

@Serializable
object Settings

@Serializable
object PlantIdentification

@Serializable
object PlantDoctor

@Serializable
object ApiKeySetup

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = PlantList // Type-safe start destination
    ) {
        composable<PlantList> { // Type-safe composable
            val viewModel: PlantViewModel = hiltViewModel()
            PlantListScreen(
                viewModel = viewModel,
                onNavigateToAddPlant = {
                    navController.navigate(AddPlant)
                },
                onNavigateToPlantDetail = { plantId ->
                    navController.navigate(PlantDetail(plantId = plantId))
                },
                onNavigateToSettings = {
                    navController.navigate(Settings)
                },
                onNavigateToPlantIdentification = {
                    navController.navigate(PlantIdentification)
                },
                onNavigateToPlantDoctor = {
                    navController.navigate(PlantDoctor)
                }
            )
        }

        composable<AddPlant> {
            val viewModel: PlantViewModel = hiltViewModel()
            AddEditPlantScreen(
                viewModel = viewModel,
                plantId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<PlantDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<PlantDetail>() // Type-safe argument retrieval
            val viewModel: PlantViewModel = hiltViewModel()
            PlantDetailScreen(
                viewModel = viewModel,
                plantId = args.plantId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(EditPlant(plantId = id))
                },
                onNavigateToGallery = { id ->
                    navController.navigate(PhotoGallery(plantId = id))
                },
                onNavigateToCareHistory = { id, name ->
                    navController.navigate(CareHistory(plantId = id, plantName = name))
                },
                onNavigateToAddActivity = { id ->
                    navController.navigate(AddCareActivity(plantId = id, plantName = ""))
                }
            )
        }

        composable<PhotoGallery> { backStackEntry ->
            val args = backStackEntry.toRoute<PhotoGallery>()
            val viewModel: PlantViewModel = hiltViewModel()
            PlantPhotoGalleryScreen(
                viewModel = viewModel,
                plantId = args.plantId,
                onNavigateBack = { navController.popBackStack() },
                onAddPhoto = {
                    navController.navigate(AddPhoto(plantId = args.plantId))
                }
            )
        }

        composable<AddPhoto> { backStackEntry ->
            val args = backStackEntry.toRoute<AddPhoto>()
            val viewModel: PlantViewModel = hiltViewModel()
            AddPhotoScreen(
                viewModel = viewModel,
                plantId = args.plantId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<EditPlant> { backStackEntry ->
            val args = backStackEntry.toRoute<EditPlant>()
            val viewModel: PlantViewModel = hiltViewModel()
            AddEditPlantScreen(
                viewModel = viewModel,
                plantId = args.plantId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Settings> {
            val viewModel: PlantViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<CareHistory> { backStackEntry ->
            val args = backStackEntry.toRoute<CareHistory>()
            val viewModel: PlantViewModel = hiltViewModel()
            CareHistoryScreen(
                plantId = args.plantId,
                plantName = args.plantName,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAddActivity = { id ->
                    navController.navigate(AddCareActivity(plantId = id, plantName = args.plantName))
                }
            )
        }

        composable<AddCareActivity> { backStackEntry ->
            val args = backStackEntry.toRoute<AddCareActivity>()
            val viewModel: PlantViewModel = hiltViewModel()
            AddCareActivityScreen(
                plantId = args.plantId,
                plantName = args.plantName,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<PlantIdentification> {
            val viewModel: PlantViewModel = hiltViewModel()
            PlantIdentificationScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddPlant = { result ->
                    viewModel.addIdentifiedPlantToCollection(result)
                    navController.popBackStack()
                },
                onNavigateToApiKeySetup = {
                    navController.navigate(ApiKeySetup)
                }
            )
        }

        composable<PlantDoctor> {
            val viewModel: PlantViewModel = hiltViewModel()
            PlantDoctorScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToApiKeySetup = {
                    navController.navigate(ApiKeySetup)
                }
            )
        }

        composable<ApiKeySetup> {
            val viewModel: PlantViewModel = hiltViewModel()
            ApiKeySetupScreen(
                preferencesManager = viewModel.preferencesManager,
                onApiKeySaved = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() },
                plantIdentificationService = viewModel.plantIdentificationService
            )
        }
    }
}

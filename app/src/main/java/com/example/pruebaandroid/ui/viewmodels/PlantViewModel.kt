package com.example.pruebaandroid.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaandroid.ai.PlantIdentificationService
import com.example.pruebaandroid.data.CareActivity
import com.example.pruebaandroid.data.CareType
import com.example.pruebaandroid.data.Plant
import com.example.pruebaandroid.data.PlantFilter
import com.example.pruebaandroid.data.PlantPhoto
import com.example.pruebaandroid.data.PlantRepository
import com.example.pruebaandroid.data.PreferencesManager
import com.example.pruebaandroid.data.SortBy
import com.example.pruebaandroid.data.models.IdentificationState
import com.example.pruebaandroid.domain.WateringCalculator
import com.example.pruebaandroid.notifications.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlantViewModel @Inject constructor(
    private val repository: PlantRepository,
    private val application: Application,
    val preferencesManager: PreferencesManager,
    val plantIdentificationService: PlantIdentificationService,
    private val wateringCalculator: WateringCalculator
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow(SortBy.NEXT_WATERING)
    val sortBy: StateFlow<SortBy> = _sortBy.asStateFlow()

    private val _filterBy = MutableStateFlow<PlantFilter>(PlantFilter.ALL)
    val filterBy: StateFlow<PlantFilter> = _filterBy.asStateFlow()

    private val _identificationState = MutableStateFlow(IdentificationState())
    val identificationState: StateFlow<IdentificationState> = _identificationState.asStateFlow()

    val allPlants: Flow<List<Plant>> = repository.allPlants

    // Combined flow for filtered and sorted plants
    val filteredAndSortedPlants = combine(
        allPlants,
        searchQuery,
        sortBy,
        filterBy
    ) { plants, query, sort, filter ->
        var filteredPlants = plants

        // Apply search filter
        if (query.isNotBlank()) {
            filteredPlants = plants.filter { plant ->
                plant.name.contains(query, ignoreCase = true) ||
                plant.species.contains(query, ignoreCase = true) ||
                plant.description.contains(query, ignoreCase = true) ||
                plant.notes.contains(query, ignoreCase = true)
            }
        }

        // Apply status filter
        filteredPlants = when (filter) {
            PlantFilter.ALL -> filteredPlants
            PlantFilter.NEEDS_WATER -> {
                val currentDate = System.currentTimeMillis()
                filteredPlants.filter { it.nextWateringDate <= currentDate }
            }
            PlantFilter.DOES_NOT_NEED_WATER -> {
                val currentDate = System.currentTimeMillis()
                filteredPlants.filter { it.nextWateringDate > currentDate }
            }
        }

        // Apply sorting
        when (sort) {
            SortBy.NAME -> filteredPlants.sortedBy { it.name.lowercase() }
            SortBy.SPECIES -> filteredPlants.sortedBy { it.species.lowercase() }
            SortBy.NEXT_WATERING -> filteredPlants.sortedBy { it.nextWateringDate }
            SortBy.LAST_WATERED -> filteredPlants.sortedByDescending { it.lastWateredDate }
        }
    }

    init {
        viewModelScope.launch {
            allPlants.collect { plants ->
                AlarmScheduler.scheduleAlarmsForPlants(application, preferencesManager, plants)
            }
        }
    }

    fun getPlantsNeedingWater(): Flow<List<Plant>> {
        val currentDate = System.currentTimeMillis()
        return repository.getPlantsNeedingWater(currentDate)
    }

    suspend fun getPlantById(id: Int): Plant? {
        return repository.getPlantById(id)
    }

    fun getPlantByIdFlow(id: Int): Flow<Plant?> {
        return repository.getPlantByIdFlow(id)
    }

    fun insertPlant(plant: Plant) = viewModelScope.launch {
        repository.insertPlant(plant)
    }

    fun updatePlant(plant: Plant) = viewModelScope.launch {
        repository.updatePlant(plant)
    }

    fun deletePlant(plant: Plant) = viewModelScope.launch {
        repository.deletePlant(plant)
    }

    fun deletePlantById(id: Int) = viewModelScope.launch {
        repository.deletePlantById(id)
    }

    fun waterPlant(plant: Plant) = viewModelScope.launch(Dispatchers.IO) {
        val currentDate = System.currentTimeMillis()
        val wateringResult = wateringCalculator.calculateNextWateringDateWithSchedule(
            plant.wateringFrequencyDays,
            plant.weekdayWateringHour,
            plant.weekdayWateringMinute,
            plant.weekendWateringHour,
            plant.weekendWateringMinute,
            plant.isIndoor
        )

        val updatedPlant = plant.copy(
            lastWateredDate = currentDate,
            nextWateringDate = wateringResult.date,
            wateringAdjustmentReason = wateringResult.adjustmentReason
        )
        repository.updatePlant(updatedPlant)
        // No need to reschedule alarms as the watering times haven't changed
        // The UI will automatically update to show which plants need watering
    }

    suspend fun calculateNextWateringDateWithSchedule(
        frequencyDays: Int,
        weekdayHour: Int,
        weekdayMinute: Int,
        weekendHour: Int,
        weekendMinute: Int,
        isIndoor: Boolean = true
    ): Long {
        val result = wateringCalculator.calculateNextWateringDateWithSchedule(
            frequencyDays,
            weekdayHour,
            weekdayMinute,
            weekendHour,
            weekendMinute,
            isIndoor
        )
        return result.date
    }

    // Photo operations
    fun getPhotosForPlant(plantId: Int): Flow<List<PlantPhoto>> {
        return repository.getPhotosForPlant(plantId)
    }

    suspend fun getPhotoById(id: Int): PlantPhoto? {
        return repository.getPhotoById(id)
    }

    fun insertPhoto(photo: PlantPhoto) = viewModelScope.launch {
        repository.insertPhoto(photo)
    }

    fun updatePhoto(photo: PlantPhoto) = viewModelScope.launch {
        repository.updatePhoto(photo)
    }

    fun deletePhoto(photo: PlantPhoto) = viewModelScope.launch {
        repository.deletePhoto(photo)
    }

    fun deletePhotoById(id: Int) = viewModelScope.launch {
        repository.deletePhotoById(id)
    }

    // Care activity operations
    fun getActivitiesForPlant(plantId: Int): Flow<List<CareActivity>> {
        return repository.getActivitiesForPlant(plantId)
    }

    suspend fun getOverdueFertilizingActivities(): List<CareActivity> {
        return repository.getOverdueActivities(CareType.FERTILIZING, System.currentTimeMillis())
    }

    suspend fun getOverdueTransplantActivities(): List<CareActivity> {
        return repository.getOverdueActivities(CareType.TRANSPLANTING, System.currentTimeMillis())
    }

    fun getUpcomingFertilizingActivities(): Flow<List<CareActivity>> {
        return repository.getUpcomingActivities(CareType.FERTILIZING, System.currentTimeMillis())
    }

    fun getUpcomingTransplantActivities(): Flow<List<CareActivity>> {
        return repository.getUpcomingActivities(CareType.TRANSPLANTING, System.currentTimeMillis())
    }

    fun getActivitiesByDateRange(startDate: Long, endDate: Long): Flow<List<CareActivity>> {
        return repository.getActivitiesByDateRange(startDate, endDate)
    }

    suspend fun getLastFertilizingActivity(plantId: Int): CareActivity? {
        return repository.getLastActivity(plantId, CareType.FERTILIZING)
    }

    suspend fun getLastTransplantActivity(plantId: Int): CareActivity? {
        return repository.getLastActivity(plantId, CareType.TRANSPLANTING)
    }

    fun fertilizePlant(plant: Plant) = viewModelScope.launch(Dispatchers.IO) {
        val currentDate = System.currentTimeMillis()
        
        // Create care activity record
        val activity = CareActivity(
            plantId = plant.id,
            activityType = CareType.FERTILIZING,
            activityDate = currentDate,
            notes = "Fertilizado con ${plant.fertilizerType}",
            nextDueDate = if (plant.fertilizationFrequencyDays != null) {
                currentDate + (plant.fertilizationFrequencyDays!! * 24 * 60 * 60 * 1000L)
            } else null,
            fertilizerType = plant.fertilizerType
        )
        
        repository.insertActivity(activity)
        
        // Update plant record
        val nextFertilizedDate = if (plant.fertilizationFrequencyDays != null) {
            currentDate + (plant.fertilizationFrequencyDays!! * 24 * 60 * 60 * 1000L)
        } else null
        
        val updatedPlant = plant.copy(
            lastFertilizedDate = currentDate,
            nextFertilizedDate = nextFertilizedDate
        )
        repository.updatePlant(updatedPlant)
    }

    fun transplantPlant(plant: Plant) = viewModelScope.launch(Dispatchers.IO) {
        val currentDate = System.currentTimeMillis()
        
        // Create care activity record
        val activity = CareActivity(
            plantId = plant.id,
            activityType = CareType.TRANSPLANTING,
            activityDate = currentDate,
            notes = "Trasplantada a maceta de ${plant.potSize}",
            nextDueDate = if (plant.transplantFrequencyMonths != null) {
                currentDate + (plant.transplantFrequencyMonths!! * 30L * 24 * 60 * 60 * 1000L)
            } else null
        )
        
        repository.insertActivity(activity)
        
        // Update plant record
        val nextTransplantDate = if (plant.transplantFrequencyMonths != null) {
            currentDate + (plant.transplantFrequencyMonths!! * 30L * 24 * 60 * 60 * 1000L)
        } else null
        
        val updatedPlant = plant.copy(
            lastTransplantedDate = currentDate,
            nextTransplantDate = nextTransplantDate
        )
        repository.updatePlant(updatedPlant)
    }

    fun getSeasonalWateringFrequency(plant: Plant): Int {
        if (!plant.useSeasonalSchedule) return plant.wateringFrequencyDays
        
        val currentMonth = java.time.LocalDate.now().monthValue
        
        return when {
            currentMonth in plant.springStartMonth..(plant.fallStartMonth - 1) -> {
                // Spring/Summer - use regular or summer frequency
                plant.summerWateringFrequency ?: plant.wateringFrequencyDays
            }
            else -> {
                // Fall/Winter - use winter frequency
                plant.winterWateringFrequency ?: plant.wateringFrequencyDays
            }
        }
    }

    // Search and filter functions
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortBy(sort: SortBy) {
        _sortBy.value = sort
    }

    fun updateFilterBy(filter: PlantFilter) {
        _filterBy.value = filter
    }

    fun insertActivity(activity: CareActivity) = viewModelScope.launch {
        repository.insertActivity(activity)
    }

    // Plant identification functions
    fun identifyPlantFromUri(imageUri: Uri) = viewModelScope.launch {
        _identificationState.value = IdentificationState(isLoading = true)
        
        plantIdentificationService.identifyPlantFromUri(imageUri)
            .onSuccess { result ->
                _identificationState.value = IdentificationState(
                    isLoading = false,
                    result = result
                )
            }
            .onFailure { exception ->
                _identificationState.value = IdentificationState(
                    isLoading = false,
                    error = exception.message ?: "Error al identificar la planta"
                )
            }
    }

    fun clearIdentificationState() {
        _identificationState.value = IdentificationState()
    }

    fun addIdentifiedPlantToCollection(result: com.example.pruebaandroid.data.models.PlantIdentificationResult) = viewModelScope.launch {
        val wateringResult = wateringCalculator.calculateNextWateringDate(7, isIndoor = true) // Default to Indoor for identified plants
        
        val newPlant = Plant(
            name = result.plantName,
            species = result.scientificName ?: "Desconocida",
            description = result.description ?: "",
            wateringFrequencyDays = 7, // Valor por defecto
            lastWateredDate = System.currentTimeMillis(),
            nextWateringDate = wateringResult.date,
            wateringAdjustmentReason = wateringResult.adjustmentReason,
            sunlightNeeds = "Partial Sun", // Valor por defecto
            notes = "Identificado con IA: ${result.description}",
            weekdayWateringHour = 8,
            weekdayWateringMinute = 0,
            weekendWateringHour = 9,
            weekendWateringMinute = 0,
            useSeasonalSchedule = false,
            springStartMonth = 3,
            fallStartMonth = 9,
            fertilizerType = "",
            fertilizationFrequencyDays = null,
            transplantFrequencyMonths = null,
            potSize = "Mediana",
            soilType = "Universal",
            humidityLevel = "",
            temperatureRange = "",
            difficultyLevel = "Beginner",
            isIndoor = true // Default
        )
        
        insertPlant(newPlant)
    }
    // Plant Diagnosis functions
    private val _diagnosisState = MutableStateFlow(com.example.pruebaandroid.data.models.DiagnosisState())
    val diagnosisState: StateFlow<com.example.pruebaandroid.data.models.DiagnosisState> = _diagnosisState.asStateFlow()

    fun diagnosePlantFromUri(imageUri: Uri) = viewModelScope.launch {
        _diagnosisState.value = com.example.pruebaandroid.data.models.DiagnosisState(isLoading = true)
        
        plantIdentificationService.diagnosePlant(imageUri)
            .onSuccess { result ->
                _diagnosisState.value = com.example.pruebaandroid.data.models.DiagnosisState(
                    isLoading = false,
                    result = result
                )
            }
            .onFailure { exception ->
                _diagnosisState.value = com.example.pruebaandroid.data.models.DiagnosisState(
                    isLoading = false,
                    error = exception.message ?: "Error al diagnosticar la planta"
                )
            }
    }

    fun clearDiagnosisState() {
        _diagnosisState.value = com.example.pruebaandroid.data.models.DiagnosisState()
    }
}

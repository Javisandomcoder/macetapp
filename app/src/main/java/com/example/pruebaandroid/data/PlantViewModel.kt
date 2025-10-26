package com.example.pruebaandroid.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebaandroid.notifications.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar

class PlantViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PlantRepository
    val allPlants: Flow<List<Plant>>

    init {
        val database = PlantDatabase.getDatabase(application)
        repository = PlantRepository(database.plantDao(), database.plantPhotoDao())
        allPlants = repository.allPlants
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
        // Reschedule alarms with new plant schedules
        AlarmScheduler.scheduleAlarmsForPlants(getApplication())
    }

    fun updatePlant(plant: Plant) = viewModelScope.launch {
        repository.updatePlant(plant)
        // Reschedule alarms with updated plant schedules
        AlarmScheduler.scheduleAlarmsForPlants(getApplication())
    }

    fun deletePlant(plant: Plant) = viewModelScope.launch {
        repository.deletePlant(plant)
        // Reschedule alarms after plant deletion
        AlarmScheduler.scheduleAlarmsForPlants(getApplication())
    }

    fun deletePlantById(id: Int) = viewModelScope.launch {
        repository.deletePlantById(id)
        // Reschedule alarms after plant deletion
        AlarmScheduler.scheduleAlarmsForPlants(getApplication())
    }

    fun waterPlant(plant: Plant) = viewModelScope.launch(Dispatchers.IO) {
        val currentDate = System.currentTimeMillis()
        val nextWateringDate = calculateNextWateringDateWithSchedule(
            plant.wateringFrequencyDays,
            plant.weekdayWateringHour,
            plant.weekdayWateringMinute,
            plant.weekendWateringHour,
            plant.weekendWateringMinute
        )

        val updatedPlant = plant.copy(
            lastWateredDate = currentDate,
            nextWateringDate = nextWateringDate
        )
        repository.updatePlant(updatedPlant)
        // No need to reschedule alarms as the watering times haven't changed
        // The UI will automatically update to show which plants need watering
    }

    fun calculateNextWateringDate(wateringFrequencyDays: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, wateringFrequencyDays)
        return calendar.timeInMillis
    }

    fun calculateNextWateringDateWithSchedule(
        wateringFrequencyDays: Int,
        weekdayHour: Int,
        weekdayMinute: Int,
        weekendHour: Int,
        weekendMinute: Int
    ): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, wateringFrequencyDays)

        // Check if the next watering date falls on a weekend
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

        // Set the appropriate hour and minute based on the day type
        val hour = if (isWeekend) weekendHour else weekdayHour
        val minute = if (isWeekend) weekendMinute else weekdayMinute
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
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
}

package com.example.pruebaandroid.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlantRepository @Inject constructor(
    private val plantDao: PlantDao,
    private val plantPhotoDao: PlantPhotoDao,
    private val careActivityDao: CareActivityDao
) {

    val allPlants: Flow<List<Plant>> = plantDao.getAllPlants()

    fun getPlantsNeedingWater(currentDate: Long): Flow<List<Plant>> {
        return plantDao.getPlantsNeedingWater(currentDate)
    }

    suspend fun getPlantById(id: Int): Plant? {
        return plantDao.getPlantById(id)
    }

    fun getPlantByIdFlow(id: Int): Flow<Plant?> {
        return plantDao.getPlantByIdFlow(id)
    }

    suspend fun insertPlant(plant: Plant): Long {
        return plantDao.insertPlant(plant)
    }

    suspend fun updatePlant(plant: Plant): Int {
        return plantDao.updatePlant(plant)
    }

    suspend fun deletePlant(plant: Plant) {
        plantDao.deletePlant(plant)
    }

    suspend fun deletePlantById(id: Int) {
        plantDao.deletePlantById(id)
    }

    // Photo operations
    fun getPhotosForPlant(plantId: Int): Flow<List<PlantPhoto>> {
        return plantPhotoDao.getPhotosForPlant(plantId)
    }

    suspend fun getPhotoById(id: Int): PlantPhoto? {
        return plantPhotoDao.getPhotoById(id)
    }

    suspend fun insertPhoto(photo: PlantPhoto): Long {
        return plantPhotoDao.insertPhoto(photo)
    }

    suspend fun updatePhoto(photo: PlantPhoto) {
        plantPhotoDao.updatePhoto(photo)
    }

    suspend fun deletePhoto(photo: PlantPhoto) {
        plantPhotoDao.deletePhoto(photo)
    }

    suspend fun deletePhotoById(id: Int) {
        plantPhotoDao.deletePhotoById(id)
    }

    // Care activity operations
    fun getActivitiesForPlant(plantId: Int): Flow<List<CareActivity>> {
        return careActivityDao.getActivitiesForPlant(plantId)
    }

    suspend fun getOverdueActivities(type: CareType, currentDate: Long): List<CareActivity> {
        return careActivityDao.getOverdueActivities(type, currentDate)
    }

    fun getUpcomingActivities(type: CareType, currentDate: Long): Flow<List<CareActivity>> {
        return careActivityDao.getUpcomingActivities(type, currentDate)
    }

    fun getActivitiesByDateRange(startDate: Long, endDate: Long): Flow<List<CareActivity>> {
        return careActivityDao.getActivitiesByDateRange(startDate, endDate)
    }

    suspend fun getActivityById(id: Int): CareActivity? {
        return careActivityDao.getActivityById(id)
    }

    suspend fun getLastActivity(plantId: Int, type: CareType): CareActivity? {
        return careActivityDao.getLastActivity(plantId, type)
    }

    suspend fun insertActivity(activity: CareActivity) {
        careActivityDao.insertActivity(activity)
    }

    suspend fun updateActivity(activity: CareActivity) {
        careActivityDao.updateActivity(activity)
    }

    suspend fun deleteActivity(activity: CareActivity) {
        careActivityDao.deleteActivity(activity)
    }

    suspend fun deleteActivityById(id: Int) {
        careActivityDao.deleteActivityById(id)
    }

    suspend fun deleteActivitiesForPlant(plantId: Int) {
        careActivityDao.deleteActivitiesForPlant(plantId)
    }

    // Statistics
    suspend fun getActivityCount(plantId: Int, type: CareType, startDate: Long): Int {
        return careActivityDao.getActivityCount(plantId, type, startDate)
    }

    suspend fun getLastActivityDate(plantId: Int, type: CareType): Long? {
        return careActivityDao.getLastActivityDate(plantId, type)
    }
}

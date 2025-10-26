package com.example.pruebaandroid.data

import kotlinx.coroutines.flow.Flow

class PlantRepository(
    private val plantDao: PlantDao,
    private val plantPhotoDao: PlantPhotoDao
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
}

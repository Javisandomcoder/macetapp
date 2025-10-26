package com.example.pruebaandroid.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantPhotoDao {
    @Query("SELECT * FROM plant_photos WHERE plantId = :plantId ORDER BY capturedDate DESC")
    fun getPhotosForPlant(plantId: Int): Flow<List<PlantPhoto>>

    @Query("SELECT * FROM plant_photos WHERE id = :id")
    suspend fun getPhotoById(id: Int): PlantPhoto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PlantPhoto): Long

    @Update
    suspend fun updatePhoto(photo: PlantPhoto)

    @Delete
    suspend fun deletePhoto(photo: PlantPhoto)

    @Query("DELETE FROM plant_photos WHERE id = :id")
    suspend fun deletePhotoById(id: Int)

    @Query("DELETE FROM plant_photos WHERE plantId = :plantId")
    suspend fun deleteAllPhotosForPlant(plantId: Int)
}

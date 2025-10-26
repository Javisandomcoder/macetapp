package com.example.pruebaandroid.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants ORDER BY nextWateringDate ASC")
    fun getAllPlants(): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE id = :id")
    suspend fun getPlantById(id: Int): Plant?

    @Query("SELECT * FROM plants WHERE id = :id")
    fun getPlantByIdFlow(id: Int): Flow<Plant?>

    @Query("SELECT * FROM plants WHERE nextWateringDate <= :currentDate ORDER BY nextWateringDate ASC")
    fun getPlantsNeedingWater(currentDate: Long): Flow<List<Plant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant): Long

    @Update
    suspend fun updatePlant(plant: Plant): Int

    @Delete
    suspend fun deletePlant(plant: Plant)

    @Query("DELETE FROM plants WHERE id = :id")
    suspend fun deletePlantById(id: Int)
}

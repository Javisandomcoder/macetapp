package com.example.pruebaandroid.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CareActivityDao {
    @Query("SELECT * FROM care_activities WHERE plantId = :plantId ORDER BY activityDate DESC")
    fun getActivitiesForPlant(plantId: Int): Flow<List<CareActivity>>

    @Query("SELECT * FROM care_activities WHERE activityType = :type AND completed = 0 AND nextDueDate <= :currentDate")
    suspend fun getOverdueActivities(type: CareType, currentDate: Long): List<CareActivity>

    @Query("SELECT * FROM care_activities WHERE activityType = :type AND nextDueDate > :currentDate ORDER BY nextDueDate ASC")
    fun getUpcomingActivities(type: CareType, currentDate: Long): Flow<List<CareActivity>>

    @Query("SELECT * FROM care_activities WHERE activityDate BETWEEN :startDate AND :endDate ORDER BY activityDate DESC")
    fun getActivitiesByDateRange(startDate: Long, endDate: Long): Flow<List<CareActivity>>

    @Query("SELECT * FROM care_activities WHERE id = :id")
    suspend fun getActivityById(id: Int): CareActivity?

    @Query("SELECT * FROM care_activities WHERE plantId = :plantId AND activityType = :type ORDER BY activityDate DESC LIMIT 1")
    suspend fun getLastActivity(plantId: Int, type: CareType): CareActivity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: CareActivity)

    @Update
    suspend fun updateActivity(activity: CareActivity)

    @Delete
    suspend fun deleteActivity(activity: CareActivity)

    @Query("DELETE FROM care_activities WHERE id = :id")
    suspend fun deleteActivityById(id: Int)

    @Query("DELETE FROM care_activities WHERE plantId = :plantId")
    suspend fun deleteActivitiesForPlant(plantId: Int)

    // Statistics queries
    @Query("SELECT COUNT(*) FROM care_activities WHERE plantId = :plantId AND activityType = :type AND activityDate >= :startDate")
    suspend fun getActivityCount(plantId: Int, type: CareType, startDate: Long): Int

    @Query("SELECT activityDate FROM care_activities WHERE plantId = :plantId AND activityType = :type ORDER BY activityDate DESC LIMIT 1")
    suspend fun getLastActivityDate(plantId: Int, type: CareType): Long?
}
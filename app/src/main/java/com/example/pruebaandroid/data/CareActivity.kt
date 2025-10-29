package com.example.pruebaandroid.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "care_activities")
data class CareActivity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val plantId: Int,
    val activityType: CareType,
    val activityDate: Long,
    val notes: String = "",
    val nextDueDate: Long? = null,
    val completed: Boolean = true,
    val fertilizerType: String? = null,
    val amount: String? = null, // e.g., "200ml", "1 scoop"
    val effectiveness: String? = null, // User rating: "Good", "Fair", "Poor"
    val beforePhoto: String? = null,
    val afterPhoto: String? = null
)

enum class CareType {
    WATERING,
    FERTILIZING,
    TRANSPLANTING,
    PRUNING,
    PEST_TREATMENT,
    DISEASE_TREATMENT,
    REPOTTING,
    MISTING,
    OTHER
}
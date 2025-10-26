package com.example.pruebaandroid.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val species: String,
    val description: String,
    val wateringFrequencyDays: Int,
    val lastWateredDate: Long,
    val nextWateringDate: Long,
    val sunlightNeeds: String, // "Full Sun", "Partial Sun", "Shade"
    val notes: String = "",
    val imageUrl: String = "",
    val weekdayWateringHour: Int = 8, // Default: 8 AM for weekdays
    val weekendWateringHour: Int = 9, // Default: 9 AM for weekends
    val weekdayWateringMinute: Int = 0, // Default: 0 minutes for weekdays
    val weekendWateringMinute: Int = 0 // Default: 0 minutes for weekends
)

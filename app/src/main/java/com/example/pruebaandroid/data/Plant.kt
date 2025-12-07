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
    val weekendWateringMinute: Int = 0, // Default: 0 minutes for weekends
    
    // Seasonal care frequencies
    val winterWateringFrequency: Int? = null, // Different frequency for winter
    val summerWateringFrequency: Int? = null, // Different frequency for summer
    
    // Fertilization tracking
    val lastFertilizedDate: Long? = null,
    val nextFertilizedDate: Long? = null,
    val fertilizationFrequencyDays: Int? = null,
    val fertilizerType: String = "", // "Liquid", "Granular", "Organic", etc.
    
    // Transplant tracking
    val lastTransplantedDate: Long? = null,
    val nextTransplantDate: Long? = null,
    val transplantFrequencyMonths: Int? = null, // How often to transplant (in months)
    val potSize: String = "", // Current pot size
    val soilType: String = "", // Soil type preference
    
    // Advanced care
    val humidityLevel: String = "", // "Low", "Medium", "High"
    val temperatureRange: String = "", // Temperature preference
    val difficultyLevel: String = "Beginner", // "Beginner", "Intermediate", "Advanced"
    
    // Seasonal schedule (when to switch frequencies)
    val useSeasonalSchedule: Boolean = false,
    val springStartMonth: Int = 3, // March (1-12)
    val fallStartMonth: Int = 9,  // September (1-12)

    // Smart Watering Features
    val isIndoor: Boolean = true, // Default to Indoor
    val wateringAdjustmentReason: String? = null // "RAIN", "HEAT", "NONE" or null
)

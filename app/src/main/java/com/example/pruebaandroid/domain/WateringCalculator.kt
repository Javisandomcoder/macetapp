package com.example.pruebaandroid.domain

import com.example.pruebaandroid.data.repository.WeatherRepository
import java.time.DayOfWeek
import java.time.ZonedDateTime
import javax.inject.Inject

class WateringCalculator @Inject constructor(
    private val weatherRepository: WeatherRepository
) {

    data class WateringResult(
        val date: Long,
        val adjustmentReason: String // "RAIN", "HEAT", "NONE"
    )

    suspend fun calculateNextWateringDate(wateringFrequencyDays: Int, isIndoor: Boolean = true): WateringResult {
        var nextDate = ZonedDateTime.now().plusDays(wateringFrequencyDays.toLong())
        var reason = "NONE"

        // Smart Watering Logic
        val weatherResult = weatherRepository.getCurrentWeather()
        weatherResult.onSuccess { response ->
            val isRaining = response.weather.any { it.main.contains("Rain", ignoreCase = true) || it.main.contains("Drizzle", ignoreCase = true) }
            val isHot = response.main.temp > 30.0

            if (!isIndoor && isRaining) {
                // Outdoor + Rain: Delay 2 days
                nextDate = nextDate.plusDays(2)
                reason = "RAIN"
            } else if (isHot) {
                // Hot > 30C: Accelerate 1 day (happens for both Indoor and Outdoor)
                nextDate = nextDate.minusDays(1)
                reason = "HEAT"
            }
        }

        return WateringResult(nextDate.toInstant().toEpochMilli(), reason)
    }

    suspend fun calculateNextWateringDateWithSchedule(
        wateringFrequencyDays: Int,
        weekdayHour: Int,
        weekdayMinute: Int,
        weekendHour: Int,
        weekendMinute: Int,
        isIndoor: Boolean
    ): WateringResult {
        var nextDate = ZonedDateTime.now().plusDays(wateringFrequencyDays.toLong())
        var reason = "NONE"

        // Smart Watering Logic
        val weatherResult = weatherRepository.getCurrentWeather()
        weatherResult.onSuccess { response ->
            val isRaining = response.weather.any { it.main.contains("Rain", ignoreCase = true) || it.main.contains("Drizzle", ignoreCase = true) }
            val isHot = response.main.temp > 30.0

            if (!isIndoor && isRaining) {
                // Outdoor + Rain: Delay 2 days
                nextDate = nextDate.plusDays(2)
                reason = "RAIN"
            } else if (isHot) {
                // Hot > 30C: Accelerate 1 day
                nextDate = nextDate.minusDays(1)
                reason = "HEAT"
            }
        }

        // Check if the next watering date falls on a weekend
        val dayOfWeek = nextDate.dayOfWeek
        val isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY

        // Set the appropriate hour and minute based on the day type
        val hour = if (isWeekend) weekendHour else weekdayHour
        val minute = if (isWeekend) weekendMinute else weekdayMinute

        val finalDate = nextDate.withHour(hour).withMinute(minute).withSecond(0).withNano(0).toInstant().toEpochMilli()
        return WateringResult(finalDate, reason)
    }
}

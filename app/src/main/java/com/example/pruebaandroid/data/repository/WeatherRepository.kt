package com.example.pruebaandroid.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.example.pruebaandroid.data.remote.WeatherResponse
import com.example.pruebaandroid.data.remote.WeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherService: WeatherService,
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // TODO: Replace with your actual OpenWeatherMap API Key
    private val apiKey = "ebace0682532cb8f55baf9d4bf07901b"

    @SuppressLint("MissingPermission")
    suspend fun getCurrentWeather(): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        try {
            // Get last known location
            val locationTask = fusedLocationClient.lastLocation
            val location: Location? = Tasks.await(locationTask)

            if (location != null) {
                val response = weatherService.getCurrentWeather(
                    lat = location.latitude,
                    lon = location.longitude,
                    apiKey = apiKey
                )
                Result.success(response)
            } else {
                Result.failure(Exception("Location not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

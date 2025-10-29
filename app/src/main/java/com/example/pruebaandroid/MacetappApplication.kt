package com.example.pruebaandroid

import android.app.Application
import android.util.Log
import com.example.pruebaandroid.data.PlantRepository
import com.example.pruebaandroid.data.PreferencesManager
import com.example.pruebaandroid.notifications.AlarmScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MacetappApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Inject
    lateinit var repository: PlantRepository

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MacetappApplication onCreate - Initializing alarms")

        // Schedule alarms for all plants on app startup
        applicationScope.launch {
            try {
                val preferencesManager = PreferencesManager.getInstance(applicationContext)
                val plants = repository.allPlants.first()

                Log.d(TAG, "Found ${plants.size} plants, scheduling alarms")
                AlarmScheduler.scheduleAlarmsForPlants(
                    applicationContext,
                    preferencesManager,
                    plants
                )
                Log.d(TAG, "Alarms scheduled successfully on app startup")
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling alarms on startup: ${e.message}", e)
            }
        }
    }

    companion object {
        private const val TAG = "MacetappApplication"
    }
}
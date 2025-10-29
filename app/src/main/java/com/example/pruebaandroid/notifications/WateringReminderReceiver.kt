package com.example.pruebaandroid.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.pruebaandroid.MainActivity
import com.example.pruebaandroid.data.PlantDatabase
import com.example.pruebaandroid.data.PlantRepository
import com.example.pruebaandroid.data.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class WateringReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: PlantRepository
    
    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "WateringReminderReceiver triggered!")
        Log.d(TAG, "Current time: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}")
        Log.d(TAG, "Intent action: ${intent.action}")

        // Handle device boot - reschedule alarms
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device boot detected - rescheduling alarms")
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val plants = repository.allPlants.first()
                    AlarmScheduler.scheduleAlarmsForPlants(context, preferencesManager, plants)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        // Get schedule information from intent
        val hour = intent.getIntExtra("HOUR", -1)
        val minute = intent.getIntExtra("MINUTE", -1)
        val isWeekend = intent.getBooleanExtra("IS_WEEKEND", false)

        Log.d(TAG, "Alarm for: ${if (isWeekend) "Weekend" else "Weekday"} ${String.format("%02d:%02d", hour, minute)}")

        // Handle regular alarm
        val pendingResult = goAsync()

        scope.launch {
            try {
                if (hour != -1 && minute != -1) {
                    checkPlantsAndNotify(context, hour, minute, isWeekend)
                } else {
                    Log.w(TAG, "Received an alarm intent with no schedule extras. This should not happen.")
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun checkPlantsAndNotify(context: Context, hour: Int, minute: Int, isWeekend: Boolean) {
        try {
            val currentDate = System.currentTimeMillis()
            val allPlants = repository.allPlants.first()

            // Filter plants that have this specific schedule and need watering
            val plantsNeedingWaterWithSchedule = allPlants.filter { plant ->
                val matchesSchedule = if (isWeekend) {
                    plant.weekendWateringHour == hour && plant.weekendWateringMinute == minute
                } else {
                    plant.weekdayWateringHour == hour && plant.weekdayWateringMinute == minute
                }
                val needsWater = plant.nextWateringDate <= currentDate
                matchesSchedule && needsWater
            }

            Log.d(TAG, "Total plants in database: ${allPlants.size}")
            Log.d(TAG, "Plants with this schedule needing water: ${plantsNeedingWaterWithSchedule.size}")

            // Log details of matching plants
            plantsNeedingWaterWithSchedule.forEachIndexed { index, plant ->
                val nextWateringDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(Date(plant.nextWateringDate))
                Log.d(TAG, "Plant ${index + 1}: ${plant.name}")
                Log.d(TAG, "  - Next watering: $nextWateringDate")
            }

            if (plantsNeedingWaterWithSchedule.isNotEmpty()) {
                Log.d(TAG, "Creating notification channel and sending notification")
                createNotificationChannel(context)
                sendNotification(context, plantsNeedingWaterWithSchedule.size)
                Log.d(TAG, "Notification sent successfully")
            } else {
                Log.d(TAG, "No plants with this schedule need watering - notification NOT sent")
            }

            // Reschedule all alarms for the next appropriate time
            val preferencesManager = PreferencesManager.getInstance(context)
            AlarmScheduler.scheduleAlarmsForPlants(context, preferencesManager, allPlants)
            Log.d(TAG, "All alarms rescheduled")
        } catch (e: Exception) {
            Log.e(TAG, "Error checking plants: ${e.message}", e)
        }

        Log.d(TAG, "WateringReminderReceiver completed")
        Log.d(TAG, "========================================")
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Riego"
            val descriptionText = "Notificaciones para recordar regar tus plantas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun sendNotification(context: Context, plantCount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationText = if (plantCount == 1) {
            "Tienes 1 planta que necesita ser regada"
        } else {
            "Tienes $plantCount plantas que necesitan ser regadas"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🌱 Recordatorio de Riego")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "WateringReminderRcvr"
        const val CHANNEL_ID = "watering_reminder_channel"
        const val NOTIFICATION_ID = 1001
    }
}

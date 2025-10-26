package com.example.pruebaandroid.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pruebaandroid.MainActivity
import com.example.pruebaandroid.R
import com.example.pruebaandroid.data.PlantDatabase
import com.example.pruebaandroid.data.PlantRepository
import kotlinx.coroutines.flow.first

class WateringReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "========================================")
        Log.d(TAG, "WateringReminderWorker started")
        Log.d(TAG, "Current time: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")

        val database = PlantDatabase.getDatabase(applicationContext)
        val repository = PlantRepository(database.plantDao(), database.plantPhotoDao())

        val currentDate = System.currentTimeMillis()
        val allPlants = repository.allPlants.first()
        val plantsNeedingWater = repository.getPlantsNeedingWater(currentDate).first()

        Log.d(TAG, "Total plants in database: ${allPlants.size}")
        Log.d(TAG, "Plants needing water: ${plantsNeedingWater.size}")

        // Log details of all plants
        allPlants.forEachIndexed { index, plant ->
            val nextWateringDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(plant.nextWateringDate))
            val needsWater = plant.nextWateringDate <= currentDate
            Log.d(TAG, "Plant ${index + 1}: ${plant.name}")
            Log.d(TAG, "  - Next watering: $nextWateringDate")
            Log.d(TAG, "  - Needs water: $needsWater")
        }

        if (plantsNeedingWater.isNotEmpty()) {
            Log.d(TAG, "Creating notification channel and sending notification")
            createNotificationChannel()
            sendNotification(plantsNeedingWater.size)
            Log.d(TAG, "Notification sent successfully")
        } else {
            Log.d(TAG, "No plants need watering - notification NOT sent")
        }

        Log.d(TAG, "WateringReminderWorker completed")
        Log.d(TAG, "========================================")

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Riego"
            val descriptionText = "Notificaciones para recordar regar tus plantas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun sendNotification(plantCount: Int) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationText = if (plantCount == 1) {
            "Tienes 1 planta que necesita ser regada"
        } else {
            "Tienes $plantCount plantas que necesitan ser regadas"
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🌱 Recordatorio de Riego")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "WateringReminderWorker"
        const val CHANNEL_ID = "watering_reminder_channel"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "watering_reminder_work"
    }
}

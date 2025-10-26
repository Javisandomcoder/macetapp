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
import com.example.pruebaandroid.data.PlantDatabase
import com.example.pruebaandroid.data.PlantRepository
import kotlinx.coroutines.flow.first

class TestNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "========================================")
        Log.d(TAG, "TestNotificationWorker started (FORCE MODE)")
        Log.d(TAG, "Current time: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")

        val database = PlantDatabase.getDatabase(applicationContext)
        val repository = PlantRepository(database.plantDao(), database.plantPhotoDao())

        val currentDate = System.currentTimeMillis()
        val allPlants = repository.allPlants.first()
        val plantsNeedingWater = repository.getPlantsNeedingWater(currentDate).first()

        Log.d(TAG, "Total plants in database: ${allPlants.size}")
        Log.d(TAG, "Plants actually needing water: ${plantsNeedingWater.size}")

        // Log details of all plants
        allPlants.forEachIndexed { index, plant ->
            val nextWateringDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(plant.nextWateringDate))
            val needsWater = plant.nextWateringDate <= currentDate
            Log.d(TAG, "Plant ${index + 1}: ${plant.name}")
            Log.d(TAG, "  - Next watering: $nextWateringDate")
            Log.d(TAG, "  - Needs water: $needsWater")
        }

        // ALWAYS send notification for testing purposes
        Log.d(TAG, "FORCE MODE: Sending test notification regardless of plant status")
        createNotificationChannel()
        sendTestNotification(allPlants.size, plantsNeedingWater.size)
        Log.d(TAG, "Test notification sent successfully")

        Log.d(TAG, "TestNotificationWorker completed")
        Log.d(TAG, "========================================")

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Riego"
            val descriptionText = "Notificaciones para recordar regar tus plantas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(WateringReminderWorker.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun sendTestNotification(totalPlants: Int, plantsNeedingWater: Int) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationText = if (plantsNeedingWater > 0) {
            if (plantsNeedingWater == 1) {
                "Tienes 1 planta que necesita ser regada"
            } else {
                "Tienes $plantsNeedingWater plantas que necesitan ser regadas"
            }
        } else {
            "Prueba exitosa: Tienes $totalPlants plantas. Todas están al día 👍"
        }

        val notification = NotificationCompat.Builder(applicationContext, WateringReminderWorker.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🌱 Notificación de Prueba")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(WateringReminderWorker.NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "TestNotificationWorker"
    }
}

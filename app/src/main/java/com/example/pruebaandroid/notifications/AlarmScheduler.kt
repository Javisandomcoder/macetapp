package com.example.pruebaandroid.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.pruebaandroid.data.Plant
import com.example.pruebaandroid.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

object AlarmScheduler {



    private const val TAG = "AlarmScheduler"

    private const val BASE_ALARM_REQUEST_CODE = 2000

    private const val ALARM_REQUEST_CODE = 1001 // For legacy single alarm



    fun scheduleAlarmsForPlants(context: Context, preferencesManager: PreferencesManager, plants: List<Plant>) {

        Log.d(TAG, "=======================================")

        Log.d(TAG, "scheduleAlarmsForPlants() called")

        Log.d(TAG, "Current time: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}")



        // Check if notifications are enabled

        val notificationsEnabled = preferencesManager.getNotificationsEnabled()

        Log.d(TAG, "Notifications enabled in preferences: $notificationsEnabled")

        if (!notificationsEnabled) {

            Log.d(TAG, "Notifications are disabled - canceling all alarms")

            cancelAllAlarms(context)

            return

        }



        Log.d(TAG, "Found ${plants.size} plants")



        // Get unique time schedules

        val uniqueSchedules = mutableSetOf<TimeSchedule>()

        plants.forEachIndexed { index, plant ->

            val weekdaySchedule = TimeSchedule(plant.weekdayWateringHour, plant.weekdayWateringMinute, isWeekend = false)

            val weekendSchedule = TimeSchedule(plant.weekendWateringHour, plant.weekendWateringMinute, isWeekend = true)

            uniqueSchedules.add(weekdaySchedule)

            uniqueSchedules.add(weekendSchedule)

            Log.d(TAG, "Plant ${index + 1}: ${plant.name}")

            Log.d(TAG, "  Weekday: ${String.format("%02d:%02d", plant.weekdayWateringHour, plant.weekdayWateringMinute)}")

            Log.d(TAG, "  Weekend: ${String.format("%02d:%02d", plant.weekendWateringHour, plant.weekendWateringMinute)}")

        }



        Log.d(TAG, "Found ${uniqueSchedules.size} unique schedules to program")



        // Cancel existing alarms

        cancelAllAlarms(context)



        // Schedule alarm for each unique time

        uniqueSchedules.forEachIndexed { index, schedule ->

            scheduleAlarmForTime(context, schedule, BASE_ALARM_REQUEST_CODE + index)

        }



        Log.d(TAG, "All alarms scheduled successfully")

        Log.d(TAG, "=======================================")

    }



    private fun scheduleAlarmForTime(context: Context, schedule: TimeSchedule, requestCode: Int) {

        val calendar = Calendar.getInstance().apply {

            val today = get(Calendar.DAY_OF_WEEK)

            val isTodayWeekend = today == Calendar.SATURDAY || today == Calendar.SUNDAY



            // Set time based on schedule type

            set(Calendar.HOUR_OF_DAY, schedule.hour)

            set(Calendar.MINUTE, schedule.minute)

            set(Calendar.SECOND, 0)

            set(Calendar.MILLISECOND, 0)



            // If we need weekend schedule but today is weekday (or vice versa), skip to appropriate day

            if (schedule.isWeekend && !isTodayWeekend) {

                // Move to next Saturday

                while (get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {

                    add(Calendar.DAY_OF_YEAR, 1)

                }

            } else if (!schedule.isWeekend && isTodayWeekend) {

                // Move to next Monday

                while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {

                    add(Calendar.DAY_OF_YEAR, 1)

                }

            } else if (timeInMillis <= System.currentTimeMillis()) {

                // Time has passed, schedule for tomorrow (or next appropriate day)

                add(Calendar.DAY_OF_YEAR, 1)

                // Make sure we're still on the right type of day

                val newDay = get(Calendar.DAY_OF_WEEK)

                val isNewDayWeekend = newDay == Calendar.SATURDAY || newDay == Calendar.SUNDAY

                if (schedule.isWeekend != isNewDayWeekend) {

                    // Adjust to correct day type

                    if (schedule.isWeekend) {

                        while (get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {

                            add(Calendar.DAY_OF_YEAR, 1)

                        }

                    } else {

                        while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {

                            add(Calendar.DAY_OF_YEAR, 1)

                        }

                    }

                }

            }

        }



        val alarmTime = calendar.timeInMillis

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        val dayType = if (schedule.isWeekend) "Weekend" else "Weekday"

        Log.d(TAG, "Scheduling alarm for $dayType at ${schedule.hour}:${schedule.minute}")

        Log.d(TAG, "  Next trigger: ${dateFormat.format(Date(alarmTime))}")



        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager



        // Check if we can schedule exact alarms (Android 12+)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            if (!alarmManager.canScheduleExactAlarms()) {

                Log.e(TAG, "Cannot schedule exact alarms - permission not granted")

                return

            }

        }



        val intent = Intent(context, WateringReminderReceiver::class.java).apply {

            putExtra("HOUR", schedule.hour)

            putExtra("MINUTE", schedule.minute)

            putExtra("IS_WEEKEND", schedule.isWeekend)

        }



        val pendingIntent = PendingIntent.getBroadcast(

            context,

            requestCode,

            intent,

            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        )



        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                alarmManager.setExactAndAllowWhileIdle(

                    AlarmManager.RTC_WAKEUP,

                    alarmTime,

                    pendingIntent

                )

            } else {

                alarmManager.setExact(

                    AlarmManager.RTC_WAKEUP,

                    alarmTime,

                    pendingIntent

                )

            }

            Log.d(TAG, "  Alarm scheduled with request code: $requestCode")

        } catch (e: Exception) {

            Log.e(TAG, "Error scheduling alarm: ${e.message}", e)

        }

    }



    /**

     * Cancels all scheduled alarms

     */

    fun cancelAllAlarms(context: Context) {

        Log.d(TAG, "cancelAllAlarms() called")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager



        // Cancel up to 100 possible alarms (should be more than enough)

        for (i in 0 until 100) {

            val intent = Intent(context, WateringReminderReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(

                context,

                BASE_ALARM_REQUEST_CODE + i,

                intent,

                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE

            )



            pendingIntent?.let {

                alarmManager.cancel(it)

                it.cancel()

            }

        }



        // Also cancel the legacy single alarm, just in case

        val singleAlarmIntent = Intent(context, WateringReminderReceiver::class.java)

        val singleAlarmPendingIntent = PendingIntent.getBroadcast(

            context,

            ALARM_REQUEST_CODE,

            singleAlarmIntent,

            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE

        )

        singleAlarmPendingIntent?.let {

            alarmManager.cancel(it)

            it.cancel()

        }



        Log.d(TAG, "All alarms canceled")

    }



    /**

     * Checks if the app can schedule exact alarms (Android 12+)

     */

    fun canScheduleExactAlarms(context: Context): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            return alarmManager.canScheduleExactAlarms()

        }

        return true // No permission needed for older versions

    }



    /**

     * Data class to represent a unique time schedule

     */

    data class TimeSchedule(

        val hour: Int,

        val minute: Int,

        val isWeekend: Boolean

    )

}



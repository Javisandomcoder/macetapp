package com.example.pruebaandroid.screens

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.pruebaandroid.MainActivity
import com.example.pruebaandroid.R
import com.example.pruebaandroid.data.PreferencesManager
import com.example.pruebaandroid.notifications.AlarmScheduler
import com.example.pruebaandroid.notifications.WateringReminderWorker
import com.example.pruebaandroid.notifications.WateringReminderReceiver
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager.getInstance(context) }
    val scope = rememberCoroutineScope()

    val notificationHour by preferencesManager.notificationHour.collectAsState()
    val notificationMinute by preferencesManager.notificationMinute.collectAsState()
    val notificationsEnabled by preferencesManager.notificationsEnabled.collectAsState()
    val isDarkMode by preferencesManager.isDarkMode.collectAsState()

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showTestSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Get plant info for debug
    var plantCount by remember { mutableStateOf(0) }
    var plantsNeedingWater by remember { mutableStateOf(0) }
    var alarmStatus by remember { mutableStateOf("Verificando...") }
    var canScheduleExact by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val database = com.example.pruebaandroid.data.PlantDatabase.getDatabase(context)
        val repository = com.example.pruebaandroid.data.PlantRepository(database.plantDao(), database.plantPhotoDao())

        repository.allPlants.collect { plants ->
            plantCount = plants.size
            plantsNeedingWater = plants.count { it.nextWateringDate <= System.currentTimeMillis() }
        }
    }

    LaunchedEffect(Unit) {
        // Check AlarmManager status
        canScheduleExact = AlarmScheduler.canScheduleExactAlarms(context)
        val isScheduled = AlarmScheduler.isAlarmScheduled(context)

        alarmStatus = when {
            !notificationsEnabled -> "❌ Desactivado"
            !canScheduleExact -> "⚠️ Sin permisos para alarmas exactas"
            isScheduled -> "✅ Programado (AlarmManager)"
            else -> "❌ No programado"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dark Mode Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Brightness4,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Apariencia",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Modo oscuro",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Activa el tema oscuro de la aplicación",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { enabled ->
                                preferencesManager.setDarkMode(enabled)
                            }
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Notificaciones",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Habilitar notificaciones",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Recibe recordatorios de riego diarios",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { enabled ->
                                preferencesManager.setNotificationsEnabled(enabled)
                                if (enabled) {
                                    AlarmScheduler.scheduleAlarm(context)
                                } else {
                                    AlarmScheduler.cancelAlarm(context)
                                }
                            }
                        )
                    }

                    if (notificationsEnabled) {
                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Hora de notificación",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Configura la hora del recordatorio diario",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { showTimePickerDialog = true }) {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = String.format("%02d:%02d", notificationHour, notificationMinute),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    if (notificationsEnabled) {
                        Divider()

                        Button(
                            onClick = {
                                testNotification(context)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Notificación de prueba enviada."
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Probar notificación ahora")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scheduleTestNotificationForced(context)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Notificación de prueba programada en 1 minuto"
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("1 min")
                            }

                            Button(
                                onClick = {
                                    AlarmScheduler.scheduleAlarm(context)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Alarma reprogramada correctamente"
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reprogramar")
                            }
                        }
                    }
                }
            }

            // Debug Card
            if (notificationsEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.BugReport,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Estado de Notificaciones",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "• Notificaciones habilitadas: Sí",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Sistema: AlarmManager (exacto)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "• Estado: $alarmStatus",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                alarmStatus.contains("❌") -> MaterialTheme.colorScheme.error
                                alarmStatus.contains("⚠️") -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                        Text(
                            text = "• Hora programada: ${String.format("%02d:%02d", notificationHour, notificationMinute)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Próxima ejecución: ${getNextExecutionTime(notificationHour, notificationMinute)}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = "• Total de plantas: $plantCount",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (plantCount == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (plantCount == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "• Plantas que necesitan riego: $plantsNeedingWater",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (plantsNeedingWater > 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (plantsNeedingWater > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        if (plantsNeedingWater == 0 && plantCount > 0) {
                            Text(
                                text = "⚠️ No hay plantas que necesiten riego ahora. Las notificaciones solo se envían cuando hay plantas pendientes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (plantCount == 0) {
                            Text(
                                text = "⚠️ No tienes plantas registradas. Agrega plantas para recibir recordatorios.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = "⚠️ IMPORTANTE: La app necesita permiso para programar alarmas exactas. Ve a Configuración del sistema y habilita 'Alarmas y recordatorios' para esta app.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = "✅ Usando AlarmManager para notificaciones EXACTAS a la hora programada.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Información",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text(
                        text = "Las notificaciones te recordarán regar las plantas que lo necesiten cada día a la hora configurada.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showTimePickerDialog) {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                preferencesManager.setNotificationTime(hour, minute)
                if (notificationsEnabled) {
                    AlarmScheduler.scheduleAlarm(context)
                }
                showTimePickerDialog = false
            },
            notificationHour,
            notificationMinute,
            true // 24-hour format
        ).apply {
            setOnCancelListener { showTimePickerDialog = false }
            show()
        }
    }
}

private fun testNotification(context: android.content.Context) {
    Log.d("SettingsScreen", "Test notification button pressed")

    // Create notification channel
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Recordatorios de Riego"
        val descriptionText = "Notificaciones para recordar regar tus plantas"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(WateringReminderReceiver.CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d("SettingsScreen", "Notification channel created")
    }

    // Create intent for notification tap
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    // Build and send notification
    val notification = NotificationCompat.Builder(context, WateringReminderReceiver.CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("🌱 Prueba de Notificación")
        .setContentText("¡Las notificaciones están funcionando correctamente!")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(WateringReminderReceiver.NOTIFICATION_ID, notification)
    Log.d("SettingsScreen", "Test notification sent")
}

private fun scheduleTestNotificationForced(context: android.content.Context) {
    Log.d("SettingsScreen", "Scheduling FORCED test notification in 1 minute")

    val testWorkRequest = OneTimeWorkRequestBuilder<com.example.pruebaandroid.notifications.TestNotificationWorker>()
        .setInitialDelay(1, TimeUnit.MINUTES)
        .build()

    WorkManager.getInstance(context).enqueue(testWorkRequest)
}

private fun getNextExecutionTime(hour: Int, minute: Int): String {
    val currentTime = Calendar.getInstance()
    val targetTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        // If the target time is before current time, it's for tomorrow
        if (before(currentTime)) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return dateFormat.format(targetTime.time)
}

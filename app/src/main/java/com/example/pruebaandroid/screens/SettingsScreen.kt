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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.pruebaandroid.MainActivity
import com.example.pruebaandroid.R
import com.example.pruebaandroid.ui.viewmodels.PlantViewModel
import com.example.pruebaandroid.data.PreferencesManager
import com.example.pruebaandroid.notifications.AlarmScheduler
import com.example.pruebaandroid.notifications.WateringReminderWorker
import com.example.pruebaandroid.notifications.WateringReminderReceiver
import com.example.pruebaandroid.ui.Theme
import com.example.pruebaandroid.ui.*
import com.example.pruebaandroid.ui.themeDescription
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PlantViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = viewModel.preferencesManager
    val scope = rememberCoroutineScope()

    val plants by viewModel.allPlants.collectAsState(initial = emptyList())
    val notificationsEnabled by preferencesManager.notificationsEnabled.collectAsState()
    val theme by preferencesManager.theme.collectAsState()
    val geminiApiKey by preferencesManager.geminiApiKey.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showApiKeySetup by remember { mutableStateOf(false) }


    var showTestSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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

                    HorizontalDivider()

                    Button(onClick = { showThemeDialog = true }) {
                        Text("Seleccionar Tema")
                    }
                }
            }

            if (showThemeDialog) {
                ThemeSelectionDialog(
                    currentTheme = theme,
                    onThemeSelected = { newTheme ->
                        preferencesManager.setTheme(newTheme)
                        showThemeDialog = false
                    },
                    onDismiss = { showThemeDialog = false }
                )
            }

            // API Key Card
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
                            Icons.Default.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Inteligencia Artificial",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Clave API de Gemini",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (geminiApiKey.isNullOrBlank()) 
                                    "No configurada" 
                                else 
                                    "Configurada ✓",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (geminiApiKey.isNullOrBlank()) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = { showApiKeySetup = true }
                        ) {
                            Text(if (geminiApiKey.isNullOrBlank()) "Configurar" else "Editar")
                        }
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

                    HorizontalDivider()

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
                                    AlarmScheduler.scheduleAlarmsForPlants(context, preferencesManager, plants)
                                } else {
                                    AlarmScheduler.cancelAllAlarms(context)
                                }
                            }
                        )
                    }

                    if (notificationsEnabled) {
                        HorizontalDivider()
                    }
                }
            }

            if (notificationsEnabled) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Diagnóstico de Notificaciones",
                            style = MaterialTheme.typography.titleMedium
                        )

                        HorizontalDivider()

                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        // Create notification channel
                                        createNotificationChannel(context)

                                        // Send test notification
                                        sendTestNotification(context)

                                        snackbarHostState.showSnackbar("Notificación de prueba enviada")
                                    } catch (e: Exception) {
                                        Log.e("SettingsScreen", "Error sending test notification", e)
                                        snackbarHostState.showSnackbar("Error: ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enviar Notificación de Prueba")
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        AlarmScheduler.scheduleAlarmsForPlants(context, preferencesManager, plants)
                                        snackbarHostState.showSnackbar("Alarmas reprogramadas (${plants.size} plantas)")
                                    } catch (e: Exception) {
                                        Log.e("SettingsScreen", "Error rescheduling alarms", e)
                                        snackbarHostState.showSnackbar("Error: ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reprogramar Todas las Alarmas")
                        }

                        val canScheduleExactAlarms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            alarmManager.canScheduleExactAlarms()
                        } else {
                            true
                        }

                        Text(
                            text = buildString {
                                append("Estado:\n")
                                append("• Plantas registradas: ${plants.size}\n")
                                append("• Permiso alarmas exactas: ${if (canScheduleExactAlarms) "✓" else "✗"}\n")
                                append("• Notificaciones habilitadas: ${if (notificationsEnabled) "✓" else "✗"}")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
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

    // API Key Setup Dialog
    if (showApiKeySetup) {
        ApiKeySetupScreen(
            preferencesManager = preferencesManager,
            onApiKeySaved = { 
                showApiKeySetup = false
                scope.launch {
                    snackbarHostState.showSnackbar("Clave API guardada correctamente")
                }
            },
            onNavigateBack = { showApiKeySetup = false },
            plantIdentificationService = viewModel.plantIdentificationService
        )
    }
}

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Recordatorios de Riego"
        val descriptionText = "Notificaciones para recordar regar tus plantas"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(WateringReminderReceiver.CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        Log.d("SettingsScreen", "Notification channel created: ${WateringReminderReceiver.CHANNEL_ID}")
    }
}

private fun sendTestNotification(context: Context) {
    Log.d("SettingsScreen", "Sending test notification")

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, WateringReminderReceiver.CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("🌱 Notificación de Prueba")
        .setContentText("Si ves esto, las notificaciones funcionan correctamente")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(9999, notification)
    Log.d("SettingsScreen", "Test notification sent with ID: 9999")
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Seleccionar Tema",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            // Calculate max height for the dialog content to fit on mobile screens
            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp
            val maxContentHeight = (screenHeight * 0.6f).coerceAtMost(400.dp)

            Column(
                modifier = Modifier.heightIn(max = maxContentHeight)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(Theme.values().size) { index ->
                        val theme = Theme.values()[index]

                        // Enhanced theme item with better visual hierarchy
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onThemeSelected(theme) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (currentTheme == theme)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface,
                                contentColor = if (currentTheme == theme)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (currentTheme == theme) 2.dp else 0.dp
                            ),
                            border = if (currentTheme == theme)
                                androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentTheme == theme,
                                    onClick = { onThemeSelected(theme) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = theme.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (currentTheme == theme) FontWeight.Bold else FontWeight.Normal,
                                        color = if (currentTheme == theme)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = theme.themeDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (currentTheme == theme)
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

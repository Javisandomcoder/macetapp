package com.example.pruebaandroid

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.pruebaandroid.data.PreferencesManager
import com.example.pruebaandroid.navigation.NavGraph
import com.example.pruebaandroid.ui.theme.PruebaAndroidTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val showAlarmPermissionDialog = mutableStateOf(false)

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
            checkAndRequestAlarmPermission()
        } else {
            Log.w(TAG, "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermission()

        setContent {
            val theme by preferencesManager.theme.collectAsStateWithLifecycle(initialValue = com.example.pruebaandroid.ui.Theme.SYSTEM)

            val useDarkTheme = when (theme) {
                com.example.pruebaandroid.ui.Theme.LIGHT -> false
                com.example.pruebaandroid.ui.Theme.DARK -> true
                com.example.pruebaandroid.ui.Theme.SYSTEM -> isSystemInDarkTheme()
                com.example.pruebaandroid.ui.Theme.MIDNIGHT -> true // Midnight is always dark
                else -> false // All other themes are light
            }

            PruebaAndroidTheme(theme = theme, darkTheme = useDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)

                    // Dialog for alarm permission
                    if (showAlarmPermissionDialog.value) {
                        AlertDialog(
                            onDismissRequest = { showAlarmPermissionDialog.value = false },
                            title = { Text(stringResource(R.string.alarm_permission_title)) },
                            text = {
                                Text(stringResource(R.string.alarm_permission_text))
                            },
                            confirmButton = {
                                Button(onClick = {
                                    openAlarmPermissionSettings()
                                    showAlarmPermissionDialog.value = false
                                }) {
                                    Text(stringResource(R.string.open_settings))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showAlarmPermissionDialog.value = false
                                }) {
                                    Text(stringResource(R.string.continue_anyway))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check permissions again when returning to the app
        checkAndRequestAlarmPermission()
    }

    private fun requestNotificationPermission() {
        Log.d(TAG, "Requesting notification permission")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Notification permission already granted")
                    checkAndRequestAlarmPermission()
                }
                else -> {
                    Log.d(TAG, "Requesting POST_NOTIFICATIONS permission")
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // No need to request on older versions
            checkAndRequestAlarmPermission()
        }
    }

    private fun checkAndRequestAlarmPermission() {
        Log.d(TAG, "Checking alarm permission")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms - showing permission dialog")
                showAlarmPermissionDialog.value = true
            } else {
                Log.d(TAG, "Can schedule exact alarms.")
            }
        } else {
            // No permission needed for Android 11 and below
            Log.d(TAG, "Android 11 or below - no exact alarm permission needed.")
        }
    }

    private fun openAlarmPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error opening alarm permission settings", e)
                // Fallback to app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
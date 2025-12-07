package com.example.pruebaandroid.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.pruebaandroid.ui.viewmodels.PlantViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDoctorScreen(
    viewModel: PlantViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToApiKeySetup: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    
    val diagnosisState by viewModel.diagnosisState.collectAsState()
    val geminiApiKey by viewModel.preferencesManager.geminiApiKey.collectAsState(initial = null)
    val hasApiKey = !geminiApiKey.isNullOrBlank()
    
    val cameraImageUri = remember {
        createImageFile(context)?.let { file ->
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            imageUri = cameraImageUri
            cameraImageUri?.let { uri ->
                scope.launch {
                    viewModel.diagnosePlantFromUri(uri)
                }
            }
        }
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val copiedUri = copyImageToAppStorage(context, it)
            copiedUri?.let { uri ->
                imageUri = uri
                scope.launch {
                    viewModel.diagnosePlantFromUri(uri)
                }
            }
        }
    }
    
    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && cameraImageUri != null) {
            cameraLauncher.launch(cameraImageUri)
        } else {
            showPermissionDialog = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doctor de Plantas") },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image capture section
            if (imageUri == null && !diagnosisState.isLoading) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (hasApiKey == null || hasApiKey == false) {
                                    "Configura tu API Key primero"
                                } else {
                                    "Toma una foto de la planta enferma"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (hasApiKey == null || hasApiKey == false) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onNavigateToApiKeySetup
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Configurar API Key")
                                }
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            when (PackageManager.PERMISSION_GRANTED) {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) -> {
                                    if (hasApiKey == null || hasApiKey == false) {
                                        showApiKeyDialog = true
                                    } else {
                                        cameraImageUri?.let { cameraLauncher.launch(it) }
                                    }
                                }
                                else -> {
                                    if (hasApiKey == null || hasApiKey == false) {
                                        showApiKeyDialog = true
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cámara")
                    }
                    
                    OutlinedButton(
                        onClick = { 
                            if (hasApiKey == null || hasApiKey == false) {
                                showApiKeyDialog = true
                            } else {
                                galleryLauncher.launch("image/*")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galería")
                    }
                }
            }
            
            // Show image and loading state
            if (imageUri != null || diagnosisState.isLoading) {
                if (imageUri != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Foto de planta enferma",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                
                // Loading state
                if (diagnosisState.isLoading) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "El Doctor Planta está analizando...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Error state
                diagnosisState.error?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { 
                                    viewModel.clearDiagnosisState()
                                    imageUri = null
                                }
                            ) {
                                Text("Intentar de nuevo")
                            }
                        }
                    }
                }
                
                // Success state
                diagnosisState.result?.let { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Problem and Severity
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = result.problem,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                val severityColor = when(result.severity.lowercase()) {
                                    "alta", "high" -> MaterialTheme.colorScheme.error
                                    "media", "medium" -> Color(0xFFFFA000) // Amber
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                
                                AssistChip(
                                    onClick = { },
                                    label = { Text(result.severity) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = severityColor
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        labelColor = severityColor
                                    )
                                )
                            }
                            
                            Divider()
                            
                            // Description
                            Text(
                                text = "Síntomas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = result.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            // Treatment
                            Text(
                                text = "Tratamiento Recomendado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    text = result.treatment,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            
                            // Prevention
                            result.prevention?.let { prevention ->
                                Text(
                                    text = "Prevención",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = prevention,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = { 
                                    viewModel.clearDiagnosisState()
                                    imageUri = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Diagnosticar otra planta")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // API Key dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("API Key Requerida") },
            text = { Text("Para usar el Doctor de Plantas necesitas configurar tu API Key de Gemini.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showApiKeyDialog = false
                        onNavigateToApiKeySetup()
                    }
                ) {
                    Text("Configurar ahora")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Permission dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permiso de cámara") },
            text = { Text("Se necesita acceso a la cámara para tomar fotos de tus plantas.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Conceder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Helper functions (duplicated from PlantIdentificationScreen - should be extracted to Utils)
private fun createImageFile(context: Context): File? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.getExternalFilesDir(null), "Pictures")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        File(storageDir, "plant_diagnosis_${timeStamp}.jpg")
    } catch (e: Exception) {
        null
    }
}

private fun copyImageToAppStorage(context: Context, sourceUri: Uri): Uri? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir("plant_diagnosis_photos")
        storageDir?.mkdirs()

        val destinationFile = File(storageDir, "DIAGNOSIS_${timeStamp}.jpg")

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        Uri.fromFile(destinationFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.pruebaandroid.data.PlantViewModel
import com.example.pruebaandroid.data.models.IdentificationState
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantIdentificationScreen(
    viewModel: PlantViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddPlant: (com.example.pruebaandroid.data.models.PlantIdentificationResult) -> Unit,
    onNavigateToApiKeySetup: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    
    val identificationState by viewModel.identificationState.collectAsState()
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
                    viewModel.identifyPlantFromUri(uri)
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
                    viewModel.identifyPlantFromUri(uri)
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
                title = { Text("Identificar Planta") },
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
            if (imageUri == null && !identificationState.isLoading) {
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
                                Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (hasApiKey == null || hasApiKey == false) {
                                    "Configura tu clave API de Gemini para empezar"
                                } else {
                                    "Toma una foto o selecciona una imagen"
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
                                    Text("Configurar Clave API")
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
            if (imageUri != null || identificationState.isLoading) {
                if (imageUri != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Imagen de planta",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                
                // Loading state
                if (identificationState.isLoading) {
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
                                text = "Identificando planta...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Error state
                identificationState.error?.let { error ->
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
                                    viewModel.clearIdentificationState()
                                    imageUri = null
                                }
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                
                // Success state
                identificationState.result?.let { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = result.plantName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            result.scientificName?.let { scientific ->
                                Text(
                                    text = scientific,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            result.description?.let { description ->
                                Text(
                                    text = "Descripción:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            // Care instructions
                            result.careInstructions?.let { care ->
                                Text(
                                    text = "Instrucciones de cuidado:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                care.watering?.let { watering ->
                                    Text(
                                        text = "💧 Riego: $watering",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                care.sunlight?.let { sunlight ->
                                    Text(
                                        text = "☀️ Luz: $sunlight",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                care.soil?.let { soil ->
                                    Text(
                                        text = "🌱 Suelo: $soil",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                care.temperature?.let { temperature ->
                                    Text(
                                        text = "🌡️ Temperatura: $temperature",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                care.humidity?.let { humidity ->
                                    Text(
                                        text = "💨 Humedad: $humidity",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { 
                                        viewModel.clearIdentificationState()
                                        imageUri = null
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Otra imagen")
                                }
                                
                                Button(
                                    onClick = { onNavigateToAddPlant(result) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Agregar a mi colección")
                                }
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
            title = { Text("Clave API requerida") },
            text = { Text("Para usar la función de identificación de plantas, necesitas configurar tu clave API de Gemini en la configuración.") },
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
            title = { Text("Permiso de cámara necesario") },
            text = { Text("Se necesita permiso para acceder a la cámara y tomar fotos de plantas.") },
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

// Helper functions (these should be the same as in AddPhotoScreen)
private fun createImageFile(context: Context): File? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(context.getExternalFilesDir(null), "Pictures")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        File(storageDir, "plant_${timeStamp}.jpg")
    } catch (e: Exception) {
        null
    }
}

private fun copyImageToAppStorage(context: Context, sourceUri: Uri): Uri? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir("plant_photos")
        storageDir?.mkdirs()

        val destinationFile = File(storageDir, "PLANT_${timeStamp}.jpg")

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
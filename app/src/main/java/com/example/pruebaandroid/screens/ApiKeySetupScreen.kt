package com.example.pruebaandroid.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pruebaandroid.data.PreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeySetupScreen(
    preferencesManager: PreferencesManager,
    onApiKeySaved: () -> Unit,
    onNavigateBack: () -> Unit,
    plantIdentificationService: com.example.pruebaandroid.ai.PlantIdentificationService
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var apiKey by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val currentApiKey by preferencesManager.geminiApiKey.collectAsState(initial = null)
    
    LaunchedEffect(currentApiKey) {
        currentApiKey?.let { apiKey = it }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar Clave API") },
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
            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "¿Por qué necesito una clave API?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Text(
                        text = "Para usar la función de identificación de plantas con IA, necesitas una clave API de Google Gemini. La aplicación es privada y tu clave solo se almacenará localmente en tu dispositivo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // API Key input
            OutlinedTextField(
                value = apiKey,
                onValueChange = { 
                    apiKey = it
                    errorMessage = null
                },
                label = { Text("Clave API de Gemini") },
                placeholder = { Text("AIzaSy...") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showApiKey) "Ocultar" else "Mostrar"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = errorMessage != null,
                supportingText = errorMessage?.let { 
                    { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            )
            
            // Instructions
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "¿Cómo obtener tu clave API?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "1. Ve a https://aistudio.google.com/app/apikey\n2. Inicia sesión con tu cuenta Google\n3. Haz clic en 'Create API Key'\n4. Activa la API de Gemini si se te solicita\n5. Copia la clave generada completa",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    
                    OutlinedButton(
                        onClick = { uriHandler.openUri("https://aistudio.google.com/app/apikey") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ir a Google AI Studio")
                    }
                }
            }
            
            // Security note
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tu clave API se almacena de forma segura y local en tu dispositivo. No se comparte con terceros.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            // Action buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (apiKey.isBlank()) {
                            errorMessage = "La clave API no puede estar vacía"
                            return@Button
                        }
                        
                        if (!apiKey.startsWith("AIzaSy")) {
                            errorMessage = "La clave API parece inválida. Debe comenzar con 'AIzaSy'"
                            return@Button
                        }
                        
                        if (apiKey.length < 30) {
                            errorMessage = "La clave API parece demasiado corta. Verifica que hayas copiado la clave completa."
                            return@Button
                        }
                        
                        isLoading = true
                        scope.launch {
                            val testResult = plantIdentificationService.testApiKey(apiKey.trim())
                            isLoading = false
                            
                            testResult.onSuccess { isValid ->
                                if (isValid) {
                                    preferencesManager.setGeminiApiKey(apiKey.trim())
                                    onApiKeySaved()
                                } else {
                                    errorMessage = "La clave API no parece funcionar correctamente. Verifica que sea válida."
                                }
                            }
                            
                            testResult.onFailure { error ->
                                errorMessage = error.message ?: "Error al validar la clave API"
                            }
                        }
                    },
                    enabled = !isLoading && apiKey.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (currentApiKey.isNullOrBlank()) "Guardar Clave API" else "Actualizar Clave API")
                }
                
                if (!currentApiKey.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            preferencesManager.setGeminiApiKey("")
                            apiKey = ""
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar Clave API")
                    }
                }
            }
        }
    }
}
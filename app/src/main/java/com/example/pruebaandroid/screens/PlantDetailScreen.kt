package com.example.pruebaandroid.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pruebaandroid.data.Plant
import com.example.pruebaandroid.data.PlantViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    viewModel: PlantViewModel,
    plantId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToGallery: (Int) -> Unit,
    onNavigateToCareHistory: (Int, String) -> Unit,
    onNavigateToAddActivity: (Int) -> Unit
) {
    val plantFromDb by viewModel.getPlantByIdFlow(plantId).collectAsState(initial = null)
    val photos by viewModel.getPhotosForPlant(plantId).collectAsState(initial = emptyList())
    val recentActivities by viewModel.getActivitiesForPlant(plantId).collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showWaterDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFertilizeDialog by remember { mutableStateOf(false) }
    var showTransplantDialog by remember { mutableStateOf(false) }

    val plant = plantFromDb

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Planta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToCareHistory(plantId, plant?.name ?: "") }) {
                        Icon(Icons.Default.History, contentDescription = "Historial de cuidado")
                    }
                    IconButton(onClick = { onNavigateToEdit(plantId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            plant?.let { currentPlant ->
                val needsWater = currentPlant.nextWateringDate <= System.currentTimeMillis()
                ExtendedFloatingActionButton(
                    onClick = { showWaterDialog = true },
                    containerColor = if (needsWater)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = if (needsWater)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.WaterDrop, contentDescription = "Regar planta")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (needsWater) "Regar ahora" else "Regar")
                }
            }
        }
    ) { paddingValues ->
        plant?.let { currentPlant ->
            val needsWater = currentPlant.nextWateringDate <= System.currentTimeMillis()

            // Animación de color para el estado
            val statusContainerColor by animateColorAsState(
                targetValue = if (needsWater)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.primaryContainer,
                animationSpec = tween(durationMillis = 600),
                label = "statusColor"
            )

            // Animación pulsante para el icono cuando necesita riego
            val infiniteTransition = rememberInfiniteTransition(label = "waterDropPulse")
            val iconScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (needsWater) 1.2f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "iconScale"
            )

            key(currentPlant.id, currentPlant.nextWateringDate) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                // Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = statusContainerColor
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (needsWater) Icons.Default.WaterDrop else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .scale(iconScale),
                            tint = if (needsWater)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (needsWater) "¡Necesita riego!" else "Planta al día",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (needsWater)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Photos preview card
                if (photos.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToGallery(plantId) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Fotos (${photos.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Ver todas",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                photos.take(3).forEach { photo ->
                                    AsyncImage(
                                        model = Uri.parse(photo.photoUri),
                                        contentDescription = "Foto de planta",
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToGallery(plantId) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Agregar fotos",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Ir a galería",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Basic Info Card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = currentPlant.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        DetailRow(
                            icon = Icons.Default.Spa,
                            label = "Especie",
                            value = currentPlant.species
                        )

                        if (currentPlant.description.isNotBlank()) {
                            DetailRow(
                                icon = Icons.Default.Description,
                                label = "Descripción",
                                value = currentPlant.description
                            )
                        }
                    }
                }

                // Advanced Care Actions Card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Acciones de Cuidado",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        // Fertilize Button
                        Button(
                            onClick = { showFertilizeDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                Icons.Default.Eco,
                                contentDescription = "Fertilizar",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fertilizar")
                        }

                        // Transplant Button
                        Button(
                            onClick = { showTransplantDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(
                                Icons.Default.Landscape,
                                contentDescription = "Trasplantar",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Trasplantar")
                        }

                        // Add Activity Button
                        OutlinedButton(
                            onClick = { onNavigateToAddActivity(plantId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Agregar actividad",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Otra Actividad")
                        }
                    }
                }

                // Fertilization Info
                if (currentPlant.lastFertilizedDate != null) {
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
                                    Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Fertilización",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Text(
                                text = "Última: ${dateFormat.format(Date(currentPlant.lastFertilizedDate))}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            currentPlant.nextFertilizedDate?.let { nextDate ->
                                Text(
                                    text = "Próxima: ${dateFormat.format(Date(nextDate))}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                // Transplant Info
                if (currentPlant.lastTransplantedDate != null) {
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
                                    Icons.Default.Landscape,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Trasplante",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Text(
                                text = "Último: ${dateFormat.format(Date(currentPlant.lastTransplantedDate))}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            currentPlant.nextTransplantDate?.let { nextDate ->
                                Text(
                                    text = "Próximo: ${dateFormat.format(Date(nextDate))}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                // Recent Activities
                if (recentActivities.take(3).isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToCareHistory(plantId, currentPlant.name) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Actividades Recientes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Ver todas",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            recentActivities.take(3).forEach { activity ->
                                ActivitySummaryRow(activity = activity)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                // Care Info Card
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Información de Cuidado",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        DetailRow(
                            icon = Icons.Default.WbSunny,
                            label = "Luz",
                            value = currentPlant.sunlightNeeds
                        )

                        DetailRow(
                            icon = Icons.Default.Schedule,
                            label = "Frecuencia de riego",
                            value = "Cada ${currentPlant.wateringFrequencyDays} días"
                        )

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Lun-Vie",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format("%02d:%02d", currentPlant.weekdayWateringHour, currentPlant.weekdayWateringMinute),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sáb-Dom",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format("%02d:%02d", currentPlant.weekendWateringHour, currentPlant.weekendWateringMinute),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        Divider()

                        DetailRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Último riego",
                            value = dateTimeFormat.format(Date(currentPlant.lastWateredDate))
                        )

                        DetailRow(
                            icon = Icons.Default.CalendarMonth,
                            label = "Próximo riego",
                            value = dateFormat.format(Date(currentPlant.nextWateringDate))
                        )
                    }
                }

                // Notes Card
                if (currentPlant.notes.isNotBlank()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Notes,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Notas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = currentPlant.notes,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Quick Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar")
                    }

                    Button(
                        onClick = { onNavigateToEdit(plantId) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar")
                    }
                }

                // Spacer to prevent FAB from covering bottom buttons
                Spacer(modifier = Modifier.height(80.dp))
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    // Water confirmation dialog
    if (showWaterDialog) {
        AlertDialog(
            onDismissRequest = { showWaterDialog = false },
            icon = {
                Icon(
                    Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Confirmar riego") },
            text = {
                plant?.let { currentPlant ->
                    val nextWateringDate = viewModel.calculateNextWateringDateWithSchedule(
                        currentPlant.wateringFrequencyDays,
                        currentPlant.weekdayWateringHour,
                        currentPlant.weekdayWateringMinute,
                        currentPlant.weekendWateringHour,
                        currentPlant.weekendWateringMinute
                    )
                    Column {
                        Text("¿Deseas marcar '${currentPlant.name}' como regada?")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Próximo riego: ${dateFormat.format(Date(nextWateringDate))} a las ${
                                java.util.Calendar.getInstance().apply { timeInMillis = nextWateringDate }.let { cal ->
                                    val isWeekend = cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SATURDAY ||
                                        cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY
                                    val hour = if (isWeekend) currentPlant.weekendWateringHour else currentPlant.weekdayWateringHour
                                    val minute = if (isWeekend) currentPlant.weekendWateringMinute else currentPlant.weekdayWateringMinute
                                    String.format("%02d:%02d", hour, minute)
                                }
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        plant?.let { currentPlant ->
                            viewModel.waterPlant(currentPlant)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "✓ ${currentPlant.name} ha sido regada",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        showWaterDialog = false
                    }
                ) {
                    Text("Regar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWaterDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar '${plant?.name}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        plant?.let {
                            viewModel.deletePlant(it)
                            onNavigateBack()
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Fertilize confirmation dialog
    if (showFertilizeDialog) {
        AlertDialog(
            onDismissRequest = { showFertilizeDialog = false },
            icon = { Icon(Icons.Default.Eco, contentDescription = "Fertilizar", tint = MaterialTheme.colorScheme.secondary) },
            title = { Text("Confirmar fertilización") },
            text = { Text("¿Deseas registrar que has fertilizado '${plant?.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        plant?.let { currentPlant ->
                            viewModel.fertilizePlant(currentPlant)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "✓ ${currentPlant.name} ha sido fertilizada",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        showFertilizeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Fertilizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFertilizeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Transplant confirmation dialog
    if (showTransplantDialog) {
        AlertDialog(
            onDismissRequest = { showTransplantDialog = false },
            icon = { Icon(Icons.Default.Landscape, contentDescription = "Trasplantar", tint = MaterialTheme.colorScheme.tertiary) },
            title = { Text("Confirmar trasplante") },
            text = { Text("¿Deseas registrar que has trasplantado '${plant?.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        plant?.let { currentPlant ->
                            viewModel.transplantPlant(currentPlant)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "✓ ${currentPlant.name} ha sido trasplantada",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                        showTransplantDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Trasplantar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransplantDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ActivitySummaryRow(activity: com.example.pruebaandroid.data.CareActivity) {
    val activityIcon = when (activity.activityType) {
        com.example.pruebaandroid.data.CareType.WATERING -> Icons.Default.WaterDrop
        com.example.pruebaandroid.data.CareType.FERTILIZING -> Icons.Default.Eco
        com.example.pruebaandroid.data.CareType.TRANSPLANTING -> Icons.Default.Landscape
        com.example.pruebaandroid.data.CareType.PRUNING -> Icons.Default.ContentCut
        com.example.pruebaandroid.data.CareType.PEST_TREATMENT -> Icons.Default.BugReport
        com.example.pruebaandroid.data.CareType.DISEASE_TREATMENT -> Icons.Default.HealthAndSafety
        com.example.pruebaandroid.data.CareType.REPOTTING -> Icons.Default.Landscape
        com.example.pruebaandroid.data.CareType.MISTING -> Icons.Default.WaterDamage
        com.example.pruebaandroid.data.CareType.OTHER -> Icons.Default.Note
    }

    val activityTypeName = when (activity.activityType) {
        com.example.pruebaandroid.data.CareType.WATERING -> "Riego"
        com.example.pruebaandroid.data.CareType.FERTILIZING -> "Fertilización"
        com.example.pruebaandroid.data.CareType.TRANSPLANTING -> "Trasplante"
        com.example.pruebaandroid.data.CareType.PRUNING -> "Poda"
        com.example.pruebaandroid.data.CareType.PEST_TREATMENT -> "Tratamiento de Plagas"
        com.example.pruebaandroid.data.CareType.DISEASE_TREATMENT -> "Tratamiento de Enfermedad"
        com.example.pruebaandroid.data.CareType.REPOTTING -> "Repotting"
        com.example.pruebaandroid.data.CareType.MISTING -> "Rociado"
        com.example.pruebaandroid.data.CareType.OTHER -> "Otro"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                activityIcon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = activityTypeName,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        Text(
            text = dateFormat.format(Date(activity.activityDate)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

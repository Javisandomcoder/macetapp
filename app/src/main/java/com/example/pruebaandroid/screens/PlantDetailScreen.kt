package com.example.pruebaandroid.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
    onNavigateToGallery: (Int) -> Unit
) {
    val plantFromDb by viewModel.getPlantByIdFlow(plantId).collectAsState(initial = null)
    val photos by viewModel.getPhotosForPlant(plantId).collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showWaterDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                                    Icons.Default.ArrowForward,
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
                                Icons.Default.ArrowForward,
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

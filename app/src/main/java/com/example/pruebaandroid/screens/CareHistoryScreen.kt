package com.example.pruebaandroid.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pruebaandroid.data.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareHistoryScreen(
    plantId: Int,
    plantName: String,
    viewModel: PlantViewModel,
    onNavigateBack: () -> Unit,
    onAddActivity: (Int) -> Unit
) {
    val activities by viewModel.getActivitiesForPlant(plantId).collectAsStateWithLifecycle(initialValue = emptyList())
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Cuidado - $plantName") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { onAddActivity(plantId) }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar actividad")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (activities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No hay registros de cuidado",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Agrega tu primera actividad de cuidado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activities) { activity ->
                    CareActivityCard(activity = activity)
                }
            }
        }
    }
}

@Composable
fun CareActivityCard(activity: CareActivity) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) }
    
    val activityIcon = when (activity.activityType) {
        CareType.WATERING -> Icons.Default.WaterDrop
        CareType.FERTILIZING -> Icons.Default.Eco
        CareType.TRANSPLANTING -> Icons.Default.Landscape
        CareType.PRUNING -> Icons.Default.ContentCut
        CareType.PEST_TREATMENT -> Icons.Default.BugReport
        CareType.DISEASE_TREATMENT -> Icons.Default.HealthAndSafety
        CareType.REPOTTING -> Icons.Default.Landscape
        CareType.MISTING -> Icons.Default.WaterDamage
        CareType.OTHER -> Icons.Default.Note
    }

    val activityColor = when (activity.activityType) {
        CareType.WATERING -> MaterialTheme.colorScheme.primary
        CareType.FERTILIZING -> MaterialTheme.colorScheme.secondary
        CareType.TRANSPLANTING -> MaterialTheme.colorScheme.tertiary
        CareType.PRUNING -> MaterialTheme.colorScheme.primaryContainer
        CareType.PEST_TREATMENT -> MaterialTheme.colorScheme.errorContainer
        CareType.DISEASE_TREATMENT -> MaterialTheme.colorScheme.error
        CareType.REPOTTING -> MaterialTheme.colorScheme.secondaryContainer
        CareType.MISTING -> MaterialTheme.colorScheme.surfaceVariant
        CareType.OTHER -> MaterialTheme.colorScheme.outline
    }

    val activityTypeName = when (activity.activityType) {
        CareType.WATERING -> "Riego"
        CareType.FERTILIZING -> "Fertilización"
        CareType.TRANSPLANTING -> "Trasplante"
        CareType.PRUNING -> "Poda"
        CareType.PEST_TREATMENT -> "Tratamiento de Plagas"
        CareType.DISEASE_TREATMENT -> "Tratamiento de Enfermedad"
        CareType.REPOTTING -> "Repotting"
        CareType.MISTING -> "Rociado"
        CareType.OTHER -> "Otro"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        activityIcon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = activityColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = activityTypeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val dateTime = Instant.ofEpochMilli(activity.activityDate)
                            .atZone(ZoneId.systemDefault())
                        Text(
                            text = "${dateTime.format(formatter)} a las ${dateTime.format(timeFormatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (!activity.completed) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Pendiente",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (activity.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = activity.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (activity.fertilizerType?.isNotBlank() == true || activity.amount?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (activity.fertilizerType?.isNotBlank() == true) {
                        Text(
                            text = "Tipo: ${activity.fertilizerType}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (activity.amount?.isNotBlank() == true) {
                        Text(
                            text = "Cantidad: ${activity.amount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (activity.nextDueDate != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val nextDate = Instant.ofEpochMilli(activity.nextDueDate)
                    .atZone(ZoneId.systemDefault())
                Text(
                    text = "Próximo: ${nextDate.format(formatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
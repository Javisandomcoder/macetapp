package com.example.pruebaandroid.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pruebaandroid.data.Plant
import com.example.pruebaandroid.data.PlantFilter
import com.example.pruebaandroid.data.PlantViewModel
import com.example.pruebaandroid.data.SortBy
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantListScreen(
    viewModel: PlantViewModel,
    onNavigateToAddPlant: () -> Unit,
    onNavigateToPlantDetail: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlantIdentification: () -> Unit
) {
    val plants by viewModel.filteredAndSortedPlants.collectAsStateWithLifecycle(initialValue = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortBy by viewModel.sortBy.collectAsStateWithLifecycle()
    val filterBy by viewModel.filterBy.collectAsStateWithLifecycle()

    // Optimize expensive calculation with remember
    val plantsNeedingWater = remember(plants) {
        plants.count { it.nextWateringDate <= System.currentTimeMillis() }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    // Add optimized LazyListState for smooth scrolling
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mis Plantas")
                        if (plantsNeedingWater > 0) {
                            Text(
                                text = "$plantsNeedingWater necesita${if (plantsNeedingWater != 1) "n" else ""} riego",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToPlantIdentification) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Identificar planta")
                    }
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
                    }
                    IconButton(onClick = { showSortDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Ordenar")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
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
            FloatingActionButton(onClick = onNavigateToAddPlant) {
                Icon(Icons.Default.Add, contentDescription = "Agregar planta")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Buscar plantas...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                singleLine = true
            )

            // Filter and sort indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (filterBy) {
                        PlantFilter.ALL -> "Todas las plantas"
                        PlantFilter.NEEDS_WATER -> "Necesitan riego"
                        PlantFilter.DOES_NOT_NEED_WATER -> "No necesitan riego"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (sortBy) {
                        SortBy.NAME -> "Ordenado por nombre"
                        SortBy.SPECIES -> "Ordenado por especie"
                        SortBy.NEXT_WATERING -> "Ordenado por próximo riego"
                        SortBy.LAST_WATERED -> "Ordenado por último riego"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (plants.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank() || filterBy != PlantFilter.ALL) {
                            "No se encontraron plantas con los filtros actuales"
                        } else {
                            "No hay plantas. Agrega tu primera planta!"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    state = listState,
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    flingBehavior = ScrollableDefaults.flingBehavior()
                ) {
                    items(
                        count = plants.size,
                        key = { index -> plants[index].id },
                        contentType = { _ -> "plant" }
                    ) { index ->
                        // Access plant by index for better performance
                        val plant = plants[index]
                        PlantCard(
                            plant = plant,
                            onPlantClick = { onNavigateToPlantDetail(plant.id) },
                            onWaterClick = {
                                viewModel.waterPlant(plant)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "✓ ${plant.name} ha sido regada",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onDeleteClick = { viewModel.deletePlant(plant) }
                        )
                    }
                }
            }
        }
        
        // Filter and Sort Dialogs
        if (showFilterDialog) {
            FilterDialog(
                currentFilter = filterBy,
                onFilterSelected = { filter ->
                    viewModel.updateFilterBy(filter)
                    showFilterDialog = false
                },
                onDismiss = { showFilterDialog = false }
            )
        }
        
        if (showSortDialog) {
            SortDialog(
                currentSort = sortBy,
                onSortSelected = { sort ->
                    viewModel.updateSortBy(sort)
                    showSortDialog = false
                },
                onDismiss = { showSortDialog = false }
            )
        }
    }
}


@Composable
fun FilterDialog(
    currentFilter: PlantFilter,
    onFilterSelected: (PlantFilter) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtrar plantas") },
        text = {
            Column {
                PlantFilter.values().forEach { filter ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onFilterSelected(filter) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentFilter == filter,
                            onClick = { onFilterSelected(filter) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (filter) {
                                PlantFilter.ALL -> "Todas las plantas"
                                PlantFilter.NEEDS_WATER -> "Necesitan riego"
                                PlantFilter.DOES_NOT_NEED_WATER -> "No necesitan riego"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun SortDialog(
    currentSort: SortBy,
    onSortSelected: (SortBy) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ordenar plantas") },
        text = {
            Column {
                SortBy.values().forEach { sort ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onSortSelected(sort) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSort == sort,
                            onClick = { onSortSelected(sort) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (sort) {
                                SortBy.NAME -> "Nombre"
                                SortBy.SPECIES -> "Especie"
                                SortBy.NEXT_WATERING -> "Próximo riego"
                                SortBy.LAST_WATERED -> "Último riego"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun PlantCard(
    plant: Plant,
    onPlantClick: () -> Unit,
    onWaterClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Ultimate performance optimization - minimal state
    val needsWater = remember(plant.nextWateringDate) {
        plant.nextWateringDate <= System.currentTimeMillis()
    }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Pre-calculate formatted dates with minimal overhead
    val formattedInfo = remember(plant.lastWateredDate, plant.nextWateringDate) {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
        val lastWatered = Instant.ofEpochMilli(plant.lastWateredDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(formatter)
        val nextWatered = Instant.ofEpochMilli(plant.nextWateringDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(formatter)
        Pair(lastWatered, nextWatered)
    }

    // Simplified for better performance - removed complex animation
    // keeping only essential functionality

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar planta") },
            text = { Text("¿Estás seguro de que deseas eliminar '${plant.name}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Cache container color calculation - will be computed inline
    // since MaterialTheme is only available in composable context

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlantClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (needsWater)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plant.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = plant.species,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalIconButton(
                        onClick = onWaterClick,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (needsWater)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = "Regar planta",
                            tint = if (needsWater)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar planta",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Último riego:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = formattedInfo.first,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (needsWater) "Necesita riego!" else "Próximo riego:",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (needsWater) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedInfo.second,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (needsWater) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (needsWater) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WaterDrop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "¡NECESITA RIEGO AHORA!",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Frecuencia: cada ${plant.wateringFrequencyDays} días | Luz: ${plant.sunlightNeeds}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

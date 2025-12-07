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
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LightMode
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
import com.example.pruebaandroid.ui.viewmodels.PlantViewModel
import com.example.pruebaandroid.data.SortBy
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.res.stringResource
import java.util.Locale
import com.example.pruebaandroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantListScreen(
    viewModel: PlantViewModel,
    onNavigateToAddPlant: () -> Unit,
    onNavigateToPlantDetail: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlantIdentification: () -> Unit,
    onNavigateToPlantDoctor: () -> Unit
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
    val context = androidx.compose.ui.platform.LocalContext.current

    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }

    // Add optimized LazyListState for smooth scrolling
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.my_plants))
                        if (plantsNeedingWater > 0) {
                            Text(
                                text = stringResource(R.string.plants_needing_water, plantsNeedingWater, if (plantsNeedingWater != 1) "n" else ""),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToPlantDoctor) {
                        Icon(Icons.Default.LocalHospital, contentDescription = "Doctor de Plantas")
                    }
                    IconButton(onClick = onNavigateToPlantIdentification) {
                        Icon(Icons.Default.CameraAlt, contentDescription = stringResource(R.string.identify_plant_desc))
                    }
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.filter_desc))
                    }
                    IconButton(onClick = { showSortDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = stringResource(R.string.sort_desc))
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_desc))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddPlant,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_plant_desc))
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
                placeholder = {
                    Text(
                        stringResource(R.string.search_plants_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_desc),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear_search_desc),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                        PlantFilter.ALL -> stringResource(R.string.filter_all)
                        PlantFilter.NEEDS_WATER -> stringResource(R.string.filter_needs_water)
                        PlantFilter.DOES_NOT_NEED_WATER -> stringResource(R.string.filter_no_needs_water)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (sortBy) {
                        SortBy.NAME -> stringResource(R.string.sort_name)
                        SortBy.SPECIES -> stringResource(R.string.sort_species)
                        SortBy.NEXT_WATERING -> stringResource(R.string.sort_next_watering)
                        SortBy.LAST_WATERED -> stringResource(R.string.sort_last_watered)
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank() || filterBy != PlantFilter.ALL) {
                                stringResource(R.string.no_plants_found)
                            } else {
                                stringResource(R.string.no_plants_yet)
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank() || filterBy != PlantFilter.ALL) {
                                stringResource(R.string.try_other_filters)
                            } else {
                                stringResource(R.string.add_first_plant)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                                        message = context.getString(R.string.plant_watered_message, plant.name),
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
        title = { Text(stringResource(R.string.filter_plants_title)) },
        text = {
            Column {
                PlantFilter.entries.forEach { filter ->
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
                                PlantFilter.ALL -> stringResource(R.string.filter_all)
                                PlantFilter.NEEDS_WATER -> stringResource(R.string.filter_needs_water)
                                PlantFilter.DOES_NOT_NEED_WATER -> stringResource(R.string.filter_no_needs_water)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
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
        title = { Text(stringResource(R.string.sort_plants_title)) },
        text = {
            Column {
                SortBy.entries.forEach { sort ->
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
                                SortBy.NAME -> stringResource(R.string.sort_option_name)
                                SortBy.SPECIES -> stringResource(R.string.sort_option_species)
                                SortBy.NEXT_WATERING -> stringResource(R.string.sort_option_next_watering)
                                SortBy.LAST_WATERED -> stringResource(R.string.sort_option_last_watered)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
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

    // Enhanced theme-aware colors with harmony
    val cardColors = if (needsWater) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
        )
    }

    // Enhanced border color based on theme
    val borderColor = if (needsWater) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    }

    // Theme-aware elevated shadow color
    val elevationColors = CardDefaults.cardElevation(
        defaultElevation = if (needsWater) 6.dp else 3.dp,
        pressedElevation = if (needsWater) 8.dp else 4.dp
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_plant_title)) },
            text = { Text(stringResource(R.string.delete_plant_confirmation, plant.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Enhanced Card with better theming
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlantClick() },
        colors = cardColors,
        elevation = elevationColors,
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(
            width = if (needsWater) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with plant name and actions
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
                        overflow = TextOverflow.Ellipsis,
                        color = if (needsWater)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = if (plant.isIndoor) 
                                MaterialTheme.colorScheme.secondaryContainer 
                            else 
                                MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = if (plant.isIndoor) "🏠" else "🌳",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        
                        Text(
                            text = plant.species,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (needsWater)
                                MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Enhanced water button with better theme integration
                    FilledTonalIconButton(
                        onClick = onWaterClick,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (needsWater) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            },
                            contentColor = if (needsWater) {
                                MaterialTheme.colorScheme.onError
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = stringResource(R.string.water_plant_desc),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Enhanced delete button
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (needsWater)
                                MaterialTheme.colorScheme.onError
                            else
                                MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_plant_desc),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Watering information with enhanced theming
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (needsWater) {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                },
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.last_watered_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (needsWater)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formattedInfo.first,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (needsWater)
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.next_watering_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (needsWater)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Smart Badge Indicator
                            if (plant.wateringAdjustmentReason == "RAIN") {
                                Text(
                                    text = "🌧️",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            } else if (plant.wateringAdjustmentReason == "HEAT") {
                                Text(
                                    text = "☀️",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                            
                            Text(
                                text = formattedInfo.second,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (needsWater)
                                    MaterialTheme.colorScheme.onErrorContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Enhanced water need indicator
            if (needsWater) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onError
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.needs_water_alert),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Plant care information with enhanced theming
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.frequency_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(R.string.frequency_value, plant.wateringFrequencyDays),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = stringResource(R.string.light_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = plant.sunlightNeeds,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.pruebaandroid.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pruebaandroid.data.Plant
import com.example.pruebaandroid.data.PlantViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlantScreen(
    viewModel: PlantViewModel,
    plantId: Int?,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var wateringFrequency by remember { mutableStateOf("") }
    var sunlightNeeds by remember { mutableStateOf("Sol Parcial") }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var weekdayHour by remember { mutableStateOf(8) }
    var weekdayMinute by remember { mutableStateOf(0) }
    var weekendHour by remember { mutableStateOf(9) }
    var weekendMinute by remember { mutableStateOf(0) }
    var expandedWeekdayHour by remember { mutableStateOf(false) }
    var expandedWeekdayMinute by remember { mutableStateOf(false) }
    var expandedWeekendHour by remember { mutableStateOf(false) }
    var expandedWeekendMinute by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val isEditMode = plantId != null

    LaunchedEffect(plantId) {
        plantId?.let { id ->
            viewModel.getPlantById(id)?.let { plant ->
                name = plant.name
                species = plant.species
                description = plant.description
                wateringFrequency = plant.wateringFrequencyDays.toString()
                sunlightNeeds = plant.sunlightNeeds
                notes = plant.notes
                weekdayHour = plant.weekdayWateringHour
                weekdayMinute = plant.weekdayWateringMinute
                weekendHour = plant.weekendWateringHour
                weekendMinute = plant.weekendWateringMinute
            }
        }
    }

    val sunlightOptions = listOf("Sol Completo", "Sol Parcial", "Sombra")
    val hourOptions = (0..23).map { hour -> String.format("%02d", hour) }
    val minuteOptions = (0..59 step 5).map { minute -> String.format("%02d", minute) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Planta" else "Agregar Planta") },
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
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it.replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase() else char.toString()
                    }
                },
                label = { Text("Nombre de la planta") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            OutlinedTextField(
                value = species,
                onValueChange = {
                    species = it.replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase() else char.toString()
                    }
                },
                label = { Text("Especie") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            OutlinedTextField(
                value = wateringFrequency,
                onValueChange = { wateringFrequency = it.filter { char -> char.isDigit() } },
                label = { Text("Frecuencia de riego (días)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Watering time schedules section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Horarios de riego",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    // Weekday watering time
                    Text(
                        text = "Lunes a Viernes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Weekday hour
                        ExposedDropdownMenuBox(
                            expanded = expandedWeekdayHour,
                            onExpandedChange = { expandedWeekdayHour = !expandedWeekdayHour },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = hourOptions[weekdayHour],
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Hora") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWeekdayHour)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedWeekdayHour,
                                onDismissRequest = { expandedWeekdayHour = false }
                            ) {
                                hourOptions.forEachIndexed { index, hour ->
                                    DropdownMenuItem(
                                        text = { Text(hour) },
                                        onClick = {
                                            weekdayHour = index
                                            expandedWeekdayHour = false
                                        }
                                    )
                                }
                            }
                        }

                        // Weekday minute
                        ExposedDropdownMenuBox(
                            expanded = expandedWeekdayMinute,
                            onExpandedChange = { expandedWeekdayMinute = !expandedWeekdayMinute },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = minuteOptions[weekdayMinute / 5],
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Minutos") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWeekdayMinute)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedWeekdayMinute,
                                onDismissRequest = { expandedWeekdayMinute = false }
                            ) {
                                minuteOptions.forEachIndexed { index, minute ->
                                    DropdownMenuItem(
                                        text = { Text(minute) },
                                        onClick = {
                                            weekdayMinute = index * 5
                                            expandedWeekdayMinute = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Weekend watering time
                    Text(
                        text = "Sábado y Domingo",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Weekend hour
                        ExposedDropdownMenuBox(
                            expanded = expandedWeekendHour,
                            onExpandedChange = { expandedWeekendHour = !expandedWeekendHour },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = hourOptions[weekendHour],
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Hora") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWeekendHour)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedWeekendHour,
                                onDismissRequest = { expandedWeekendHour = false }
                            ) {
                                hourOptions.forEachIndexed { index, hour ->
                                    DropdownMenuItem(
                                        text = { Text(hour) },
                                        onClick = {
                                            weekendHour = index
                                            expandedWeekendHour = false
                                        }
                                    )
                                }
                            }
                        }

                        // Weekend minute
                        ExposedDropdownMenuBox(
                            expanded = expandedWeekendMinute,
                            onExpandedChange = { expandedWeekendMinute = !expandedWeekendMinute },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = minuteOptions[weekendMinute / 5],
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Minutos") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWeekendMinute)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expandedWeekendMinute,
                                onDismissRequest = { expandedWeekendMinute = false }
                            ) {
                                minuteOptions.forEachIndexed { index, minute ->
                                    DropdownMenuItem(
                                        text = { Text(minute) },
                                        onClick = {
                                            weekendMinute = index * 5
                                            expandedWeekendMinute = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = sunlightNeeds,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Necesidades de luz") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    sunlightOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                sunlightNeeds = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas adicionales") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            Button(
                onClick = {
                    if (name.isNotBlank() && species.isNotBlank() && wateringFrequency.isNotBlank()) {
                        scope.launch {
                            val currentDate = System.currentTimeMillis()
                            val wateringFrequencyDays = wateringFrequency.toIntOrNull() ?: 7

                            // Para plantas nuevas: establecer que necesitan riego HOY
                            // Para plantas editadas: mantener las fechas existentes
                            val (lastWatered, nextWatering) = if (isEditMode) {
                                // Modo edición: recalcular basándose en la última vez que se regó
                                val existingPlant = viewModel.getPlantById(plantId!!)
                                if (existingPlant != null) {
                                    // Mantener la última fecha de riego y recalcular la próxima
                                    val calendar = java.util.Calendar.getInstance()
                                    calendar.timeInMillis = existingPlant.lastWateredDate
                                    calendar.add(java.util.Calendar.DAY_OF_YEAR, wateringFrequencyDays)
                                    Pair(existingPlant.lastWateredDate, calendar.timeInMillis)
                                } else {
                                    // Si no se encuentra, usar valores por defecto
                                    Pair(currentDate, currentDate)
                                }
                            } else {
                                // Modo creación: establecer que necesita riego HOY
                                // lastWateredDate = hace X días (según frecuencia)
                                // nextWateringDate = HOY
                                val calendar = java.util.Calendar.getInstance()
                                calendar.add(java.util.Calendar.DAY_OF_YEAR, -wateringFrequencyDays)
                                val lastWateredDate = calendar.timeInMillis
                                Pair(lastWateredDate, currentDate)
                            }

                            val plant = Plant(
                                id = plantId ?: 0,
                                name = name,
                                species = species,
                                description = description,
                                wateringFrequencyDays = wateringFrequencyDays,
                                lastWateredDate = lastWatered,
                                nextWateringDate = nextWatering,
                                sunlightNeeds = sunlightNeeds,
                                notes = notes,
                                weekdayWateringHour = weekdayHour,
                                weekdayWateringMinute = weekdayMinute,
                                weekendWateringHour = weekendHour,
                                weekendWateringMinute = weekendMinute
                            )

                            if (isEditMode) {
                                viewModel.updatePlant(plant)
                            } else {
                                viewModel.insertPlant(plant)
                            }
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && species.isNotBlank() && wateringFrequency.isNotBlank()
            ) {
                Text(if (isEditMode) "Actualizar" else "Guardar")
            }
        }
    }
}

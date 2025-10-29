package com.example.pruebaandroid.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pruebaandroid.data.CareActivity
import com.example.pruebaandroid.data.CareType
import com.example.pruebaandroid.data.PlantViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCareActivityScreen(
    plantId: Int,
    plantName: String,
    viewModel: PlantViewModel,
    onNavigateBack: () -> Unit
) {
    var activityType by remember { mutableStateOf(CareType.WATERING) }
    var notes by remember { mutableStateOf("") }
    var fertilizerType by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var frequencyDays by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var expandedFertilizerType by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Actividad de Cuidado") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
            // Plant Info
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Planta: $plantName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Activity Type Selection
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Tipo de Actividad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = when (activityType) {
                                CareType.WATERING -> "Riego"
                                CareType.FERTILIZING -> "Fertilización"
                                CareType.TRANSPLANTING -> "Trasplante"
                                CareType.PRUNING -> "Poda"
                                CareType.PEST_TREATMENT -> "Tratamiento de Plagas"
                                CareType.DISEASE_TREATMENT -> "Tratamiento de Enfermedad"
                                CareType.REPOTTING -> "Repotting"
                                CareType.MISTING -> "Rociado"
                                CareType.OTHER -> "Otro"
                            },
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            CareType.values().forEach { type ->
                                val typeName = when (type) {
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
                                DropdownMenuItem(
                                    text = { Text(typeName) },
                                    onClick = {
                                        activityType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                minLines = 3,
                maxLines = 5
            )

            // Fertilizer Type (only for fertilizing)
            if (activityType == CareType.FERTILIZING) {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Detalles de Fertilización",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Fertilizer Type
                        ExposedDropdownMenuBox(
                            expanded = expandedFertilizerType,
                            onExpandedChange = { expandedFertilizerType = !expandedFertilizerType }
                        ) {
                            OutlinedTextField(
                                value = fertilizerType.ifBlank { "Seleccionar tipo" },
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFertilizerType) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expandedFertilizerType,
                                onDismissRequest = { expandedFertilizerType = false }
                            ) {
                                val fertilizerTypes = listOf("Líquido", "Granulado", "Orgánico", "Químico", "Espigas")
                                fertilizerTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            fertilizerType = type
                                            expandedFertilizerType = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Amount
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Cantidad (ej: 200ml, 1 cucharada)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Frequency
                        OutlinedTextField(
                            value = frequencyDays,
                            onValueChange = { frequencyDays = it },
                            label = { Text("Frecuencia (días para próximo)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    scope.launch {
                        val nextDueDate = if (activityType == CareType.FERTILIZING && frequencyDays.isNotBlank()) {
                            System.currentTimeMillis() + (frequencyDays.toLong() * 24 * 60 * 60 * 1000)
                        } else null

                        val activity = CareActivity(
                            plantId = plantId,
                            activityType = activityType,
                            activityDate = System.currentTimeMillis(),
                            notes = notes,
                            nextDueDate = nextDueDate,
                            completed = true,
                            fertilizerType = if (activityType == CareType.FERTILIZING) fertilizerType else null,
                            amount = if (activityType == CareType.FERTILIZING) amount else null
                        )

                        viewModel.insertActivity(activity)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Actividad")
            }
        }
    }
}
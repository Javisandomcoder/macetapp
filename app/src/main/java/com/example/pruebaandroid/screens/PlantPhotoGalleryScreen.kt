package com.example.pruebaandroid.screens

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pruebaandroid.data.PlantPhoto
import com.example.pruebaandroid.data.PlantViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantPhotoGalleryScreen(
    viewModel: PlantViewModel,
    plantId: Int,
    onNavigateBack: () -> Unit,
    onAddPhoto: () -> Unit
) {
    val photos by viewModel.getPhotosForPlant(plantId).collectAsState(initial = emptyList())
    var selectedPhoto by remember { mutableStateOf<PlantPhoto?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galería de Fotos") },
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPhoto) {
                Icon(Icons.Default.AddAPhoto, contentDescription = "Agregar foto")
            }
        }
    ) { paddingValues ->
        if (photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No hay fotos todavía",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Toca el botón + para agregar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photos) { photo ->
                    PhotoGridItem(
                        photo = photo,
                        onClick = { selectedPhoto = photo },
                        onDelete = {
                            selectedPhoto = photo
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    // Photo detail dialog
    selectedPhoto?.let { photo ->
        if (!showDeleteDialog) {
            PhotoDetailDialog(
                photo = photo,
                onDismiss = { selectedPhoto = null },
                onDelete = {
                    showDeleteDialog = true
                }
            )
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && selectedPhoto != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedPhoto = null
            },
            title = { Text("Eliminar foto") },
            text = { Text("¿Estás seguro de que deseas eliminar esta foto?") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedPhoto?.let { viewModel.deletePhoto(it) }
                        showDeleteDialog = false
                        selectedPhoto = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    selectedPhoto = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PhotoGridItem(
    photo: PlantPhoto,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        Box {
            AsyncImage(
                model = Uri.parse(photo.photoUri),
                contentDescription = photo.description.ifEmpty { "Foto de planta" },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Delete button overlay
            IconButton(
                onClick = { onDelete() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailDialog(
    photo: PlantPhoto,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss
    ) {
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                AsyncImage(
                    model = Uri.parse(photo.photoUri),
                    contentDescription = photo.description.ifEmpty { "Foto de planta" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (photo.description.isNotBlank()) {
                    Text(
                        text = photo.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = "Capturada: ${dateFormat.format(Date(photo.capturedDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDelete,
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
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

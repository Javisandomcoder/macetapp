package com.example.pruebaandroid.data.models

data class PlantIdentificationResult(
    val plantName: String,
    val scientificName: String? = null,
    val confidence: Float? = null,
    val description: String? = null,
    val careInstructions: CareInstructions? = null,
    val imageUrl: String? = null
)

data class CareInstructions(
    val watering: String? = null,
    val sunlight: String? = null,
    val soil: String? = null,
    val temperature: String? = null,
    val humidity: String? = null,
    val fertilizing: String? = null,
    val pruning: String? = null,
    val commonIssues: String? = null
)

data class IdentificationState(
    val isLoading: Boolean = false,
    val result: PlantIdentificationResult? = null,
    val error: String? = null
)
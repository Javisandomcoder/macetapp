package com.example.pruebaandroid.data.models

data class DiagnosisState(
    val isLoading: Boolean = false,
    val result: PlantDiagnosisResult? = null,
    val error: String? = null
)

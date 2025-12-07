package com.example.pruebaandroid.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PlantDiagnosisResult(
    val problem: String,
    val severity: String, // "Low", "Medium", "High"
    val description: String,
    val treatment: String,
    val prevention: String? = null
)

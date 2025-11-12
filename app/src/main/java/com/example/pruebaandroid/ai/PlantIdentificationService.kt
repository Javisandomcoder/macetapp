package com.example.pruebaandroid.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.pruebaandroid.data.models.CareInstructions
import com.example.pruebaandroid.data.models.PlantIdentificationResult
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

class PlantIdentificationService(
    private val context: Context,
    private val getApiKey: () -> String?
) {
    
    private fun getGenerativeModel(): GenerativeModel? {
        val apiKey = getApiKey() ?: return null
        return GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = apiKey
        )
    }
    
    suspend fun testApiKey(apiKey: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val testModel = GenerativeModel(
                    modelName = "gemini-2.5-flash",
                    apiKey = apiKey
                )
                
                val testPrompt = "Responde solo con 'OK' si puedes recibir este mensaje."
                val response = testModel.generateContent(testPrompt)
                val result = response.text == "OK"
                Result.success(result)
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("403") == true || e.message?.contains("401") == true ->
                        "Clave API inválida o expirada."
                    e.message?.contains("404") == true ->
                        "Modelo gemini-2.5-flash no disponible para esta clave API."
                    e.message?.contains("quota") == true ->
                        "Cuota de API excedida. Intenta más tarde."
                    e.message?.contains("network") == true || e.message?.contains("connection") == true ->
                        "Error de conexión. Verifica tu internet."
                    else -> "Error al validar clave API: ${e.message}"
                }
                Result.failure(Exception(errorMessage))
            }
        }
    }
    
    suspend fun identifyPlantFromUri(imageUri: Uri): Result<PlantIdentificationResult> {
        return try {
            val bitmap = uriToBitmap(imageUri) ?: return Result.failure(IOException("No se pudo cargar la imagen"))
            identifyPlantFromBitmap(bitmap)
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo procesar la imagen: ${e.message}"))
        }
    }
    
    suspend fun identifyPlantFromBitmap(bitmap: Bitmap): Result<PlantIdentificationResult> {
        return withContext(Dispatchers.IO) {
            try {
                val generativeModel = getGenerativeModel() 
                    ?: return@withContext Result.failure(Exception("No hay una clave API de Gemini configurada"))
                
                val prompt = """
                    Analiza esta imagen e identifica qué planta es. Proporciona la siguiente información:
                    
                    1. Nombre común de la planta
                    2. Nombre científico
                    3. Breve descripción
                    4. Instrucciones de cuidado detalladas incluyendo:
                       - Riego
                       - Luz solar
                       - Tipo de suelo
                       - Temperatura ideal
                       - Humedad
                       - Fertilización
                       - Poda
                       - Problemas comunes
                    
                    Responde en formato JSON con la siguiente estructura:
                    {
                        "plantName": "nombre común",
                        "scientificName": "nombre científico",
                        "description": "descripción breve",
                        "careInstructions": {
                            "watering": "instrucciones de riego",
                            "sunlight": "requerimientos de luz",
                            "soil": "tipo de suelo",
                            "temperature": "temperatura ideal",
                            "humidity": "requerimientos de humedad",
                            "fertilizing": "fertilización",
                            "pruning": "poda",
                            "commonIssues": "problemas comunes"
                        }
                    }
                """.trimIndent()
                
                val inputContent = content {
                    image(bitmap)
                    text(prompt)
                }
                
                val response = try {
                    generativeModel.generateContent(inputContent)
                } catch (e: Exception) {
                    return@withContext Result.failure(
                        Exception("Error de API de Gemini: ${e.message ?: "Error desconocido"}")
                    )
                }
                
                val responseText = response.text ?: return@withContext Result.failure(
                    Exception("No se obtuvo respuesta válida de Gemini. Verifica tu clave API.")
                )
                
                // Parsear la respuesta JSON
                val result = parseGeminiResponse(responseText)
                Result.success(result)
                
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("404") == true -> 
                        "Modelo no disponible. Es posible que necesites una clave API diferente."
                    e.message?.contains("403") == true || e.message?.contains("401") == true ->
                        "Clave API inválida o expirada. Por favor, verifica tu clave."
                    e.message?.contains("quota") == true ->
                        "Cuota de API excedida. Intenta más tarde."
                    e.message?.contains("network") == true || e.message?.contains("connection") == true ->
                        "Error de conexión. Verifica tu internet e intenta de nuevo."
                    else -> "Error al identificar la planta: ${e.message}"
                }
                Result.failure(Exception(errorMessage))
            }
        }
    }
    
    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseGeminiResponse(responseText: String): PlantIdentificationResult {
        // Este es un parser simplificado. En producción, deberías usar una librería JSON robusta
        return try {
            // Eliminar caracteres de formato markdown si existen
            val cleanJson = responseText.replace("```json", "").replace("```", "").trim()
            
            // Usar kotlinx.serialization para parsear el JSON
            // Por ahora, una implementación simple:
            parseJsonManually(cleanJson)
        } catch (e: Exception) {
            // Si falla el parsing, devolver un resultado básico
            PlantIdentificationResult(
                plantName = "Planta no identificada",
                description = "No se pudo identificar la planta. Por favor, intenta con otra foto.",
                careInstructions = CareInstructions(
                    watering = "No disponible",
                    sunlight = "No disponible"
                )
            )
        }
    }
    
    private fun parseJsonManually(json: String): PlantIdentificationResult {
        // Parser manual simple - en producción usar kotlinx.serialization
        val plantName = extractValue(json, "plantName") ?: "Planta desconocida"
        val scientificName = extractValue(json, "scientificName")
        val description = extractValue(json, "description")
        
        val careInstructions = CareInstructions(
            watering = extractValue(json, "watering"),
            sunlight = extractValue(json, "sunlight"),
            soil = extractValue(json, "soil"),
            temperature = extractValue(json, "temperature"),
            humidity = extractValue(json, "humidity"),
            fertilizing = extractValue(json, "fertilizing"),
            pruning = extractValue(json, "pruning"),
            commonIssues = extractValue(json, "commonIssues")
        )
        
        return PlantIdentificationResult(
            plantName = plantName,
            scientificName = scientificName,
            description = description,
            careInstructions = careInstructions
        )
    }
    
    private fun extractValue(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        return pattern.find(json)?.groupValues?.get(1)
    }
}
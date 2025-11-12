package com.example.pruebaandroid.di

import android.content.Context
import com.example.pruebaandroid.ai.PlantIdentificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {
    
    @Provides
    @Singleton
    fun providePlantIdentificationService(
        @ApplicationContext context: Context,
        preferencesManager: com.example.pruebaandroid.data.PreferencesManager
    ): PlantIdentificationService {
        return PlantIdentificationService(context) {
            preferencesManager.geminiApiKey.value
        }
    }
}
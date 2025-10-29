package com.example.pruebaandroid.di

import android.content.Context
import androidx.room.Room
import com.example.pruebaandroid.data.CareActivityDao
import com.example.pruebaandroid.data.PlantDatabase
import com.example.pruebaandroid.data.PlantDao
import com.example.pruebaandroid.data.PlantPhotoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePlantDatabase(@ApplicationContext context: Context): PlantDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            PlantDatabase::class.java,
            "plant_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providePlantDao(database: PlantDatabase): PlantDao {
        return database.plantDao()
    }

    @Provides
    fun providePlantPhotoDao(database: PlantDatabase): PlantPhotoDao {
        return database.plantPhotoDao()
    }

    @Provides
    fun provideCareActivityDao(database: PlantDatabase): CareActivityDao {
        return database.careActivityDao()
    }
}
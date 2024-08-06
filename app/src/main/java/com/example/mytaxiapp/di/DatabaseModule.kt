package com.example.mytaxiapp.di

import android.content.Context
import androidx.room.Room
import com.example.mytaxiapp.MyApp
import com.example.mytaxiapp.features.home.data.database.UserLocationDao
import com.example.mytaxiapp.features.home.data.repositoryImpl.UserLocationRepositoryImpl
import com.example.mytaxiapp.features.home.domain.repository.UserLocationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    abstract fun bindUserLocationRepository(
        userLocationRepositoryImpl: UserLocationRepositoryImpl
    ): UserLocationRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "app_database.db"
            ).fallbackToDestructiveMigration().build()
        }

        @Provides
        @Singleton
        fun provideUserLocationDao(database: AppDatabase): UserLocationDao {
            return database.userLocationDao()
        }
    }
}

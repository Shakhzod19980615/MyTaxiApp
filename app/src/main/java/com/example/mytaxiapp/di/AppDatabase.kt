package com.example.mytaxiapp.di

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mytaxiapp.features.home.data.database.UserLocationDao
import com.example.mytaxiapp.features.home.data.database.UserLocationEntity

@Database(entities = [UserLocationEntity::class], version = 1,exportSchema = true)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userLocationDao(): UserLocationDao
}
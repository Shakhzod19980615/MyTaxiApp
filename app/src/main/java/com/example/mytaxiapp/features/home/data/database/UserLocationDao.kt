package com.example.mytaxiapp.features.home.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserLocationDao {
    @Insert
    suspend fun insert(location: UserLocationEntity)

    @Query("SELECT * FROM locations ORDER BY id DESC LIMIT 1")
    fun getLastLocation(): Flow<UserLocationEntity?>
}
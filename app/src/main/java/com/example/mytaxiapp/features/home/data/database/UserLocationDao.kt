package com.example.mytaxiapp.features.home.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
@Dao
interface UserLocationDao {
    @Insert
    suspend fun insert(location: UserLocationEntity)

    @Query("SELECT * FROM locations ORDER BY id DESC LIMIT 1")
    suspend fun getLastLocation(): UserLocationEntity?
}
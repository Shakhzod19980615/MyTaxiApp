package com.example.mytaxiapp.features.home.data.repositoryImpl

import com.example.mytaxiapp.features.home.data.database.UserLocationDao
import com.example.mytaxiapp.features.home.data.dto.toUserLocationEntity
import com.example.mytaxiapp.features.home.data.dto.toUserLocationModel
import com.example.mytaxiapp.features.home.domain.model.UserLocationModel
import com.example.mytaxiapp.features.home.domain.repository.UserLocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

class UserLocationRepositoryImpl @Inject constructor(
    private val userLocationDao: UserLocationDao
):UserLocationRepository {
    override fun getLastLocation(): Flow<UserLocationModel> {
        return userLocationDao.getLastLocation().mapNotNull { it?.toUserLocationModel() }
    }

    override suspend fun insert(location: UserLocationModel) {
        return userLocationDao.insert(location.toUserLocationEntity())
    }
}
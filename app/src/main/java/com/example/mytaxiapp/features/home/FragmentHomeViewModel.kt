package com.example.mytaxiapp.features.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.mytaxi.LocationService
import com.example.mytaxiapp.features.home.domain.model.UserLocationModel
import com.example.mytaxiapp.features.home.domain.use_case.UserLocationUseCase
import com.example.mytaxiapp.features.home.presentation.LocationIntent
import com.example.mytaxiapp.features.home.presentation.LocationState
import com.example.mytaxiapp.features.home.presentation.LocationViewState
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FragmentHomeViewModel @Inject constructor(
    private val userLocationUseCase: UserLocationUseCase,
    @ApplicationContext private val context: Context
): ViewModel() {
    private var mapboxMap: MapboxMap? = null
    @OptIn(ExperimentalMaterial3Api::class)
    private val _bottomSheetState = MutableStateFlow<SheetValue>(SheetValue.PartiallyExpanded) // Initial state
    @OptIn(ExperimentalMaterial3Api::class)
    val bottomSheetState: StateFlow<SheetValue> get() = _bottomSheetState.asStateFlow()
    private val _state = MutableStateFlow(LocationState())
    val state: StateFlow<LocationState> get() = _state

    private val _viewState = MutableStateFlow(LocationViewState())
    val viewState: StateFlow<LocationViewState> get() = _viewState

    private val intentFlow = MutableSharedFlow<LocationIntent>()
    init {
        processIntents()
        registerLocationReceiver()
        startLocationService()
    }
    fun setMapboxMap(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
    }
    private fun startLocationService() {
        val intent = Intent(context, LocationService::class.java)
        context.startService(intent)
    }

    private fun stopLocationService() {
        val intent = Intent(context, LocationService::class.java)
        context.stopService(intent)
    }
    private fun registerLocationReceiver() {
        val filter = IntentFilter("LocationUpdate")
        LocalBroadcastManager.getInstance(context).registerReceiver(locationReceiver, filter)
    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val latitude = intent?.getDoubleExtra("latitude", 0.0)
            val longitude = intent?.getDoubleExtra("longitude", 0.0)
            if (latitude != null && longitude != null) {
                viewModelScope.launch {
                    intentFlow.emit(LocationIntent.UpdateLocation(latitude, longitude))
                    _viewState.value.currentLocation?.let { location ->
                        val latLng = LatLng(location.latitude, location.longitude)
                        mapboxMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0))
                    }
                }
            }
        }
    }
    private fun processIntents() {
        viewModelScope.launch {
            intentFlow.collect { intent ->
                when (intent) {
                    is LocationIntent.FetchLastLocation -> fetchLastLocation()
                    is LocationIntent.UpdateLocation -> updateLocation(intent.latitude, intent.longitude)
                }
            }
        }
    }

    private fun fetchLastLocation() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val location = userLocationUseCase.getLastLocation()
                _state.value = _state.value.copy(isLoading = false, location = location)
                _viewState.value = LocationViewState(currentLocation = location)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        val locationModel = UserLocationModel(latitude, longitude)
        viewModelScope.launch {
            _state.value = LocationState(
                isLoading = false,
                location = UserLocationModel(latitude, longitude),
                error = null
            )
        }
        _viewState.value = LocationViewState(currentLocation = locationModel)
        _state.value = _state.value.copy(location = locationModel)
        saveLocationToDatabase(locationModel)
    }
    private fun saveLocationToDatabase(location: UserLocationModel) {
        viewModelScope.launch {
            userLocationUseCase.insert(location)
        }
    }

    override fun onCleared() {
        super.onCleared()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(locationReceiver)
        stopLocationService()
    }
    @OptIn(ExperimentalMaterial3Api::class)
    fun setBottomSheetState(sheetState: SheetValue) {
        viewModelScope.launch {
            _bottomSheetState.emit(sheetState)
        }
    }
}
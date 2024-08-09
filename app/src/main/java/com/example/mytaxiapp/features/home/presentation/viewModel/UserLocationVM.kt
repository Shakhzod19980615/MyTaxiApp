package com.example.mytaxiapp.features.home.presentation.viewModel

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytaxiapp.common.LocationService
import com.example.mytaxiapp.features.home.domain.model.UserLocationModel
import com.example.mytaxiapp.features.home.domain.use_case.UserLocationUseCase
import com.example.mytaxiapp.features.home.presentation.intent.LocationState
import com.mapbox.mapboxsdk.maps.MapboxMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserLocationVM @Inject constructor(
    private val userLocationUseCase: UserLocationUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private var mapboxMap: MapboxMap? = null

    @OptIn(ExperimentalMaterial3Api::class)
    private val _bottomSheetState =
        MutableStateFlow<SheetValue>(SheetValue.PartiallyExpanded) // Initial state

    @OptIn(ExperimentalMaterial3Api::class)
    val bottomSheetState: StateFlow<SheetValue> get() = _bottomSheetState.asStateFlow()
    private val _state = MutableStateFlow(LocationState())
    val state: StateFlow<LocationState> get() = _state



    init {
        viewModelScope.launch {
            userLocationUseCase.getLastLocation().collectLatest {
                _state.value = _state.value.copy(location = it)
            }
        }
        //startLocationService()
    }


    fun setMapboxMap(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
    }

   /* private fun startLocationService() {
        val intent = Intent(context, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
*/
    private fun stopLocationService() {
        val intent = Intent(context, LocationService::class.java)
        context.stopService(intent)
    }



    override fun onCleared() {
        super.onCleared()
        stopLocationService()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun setBottomSheetState(sheetState: SheetValue) {
        viewModelScope.launch {
            _bottomSheetState.emit(sheetState)
        }
    }
}
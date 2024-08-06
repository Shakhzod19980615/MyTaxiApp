package com.example.mytaxiapp.features.viewModel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FragmentHomeViewModel @Inject constructor(): ViewModel() {
    @OptIn(ExperimentalMaterial3Api::class)
    private val _bottomSheetState = MutableStateFlow<SheetValue>(SheetValue.PartiallyExpanded) // Initial state
    @OptIn(ExperimentalMaterial3Api::class)
    val bottomSheetState: StateFlow<SheetValue> get() = _bottomSheetState.asStateFlow()

    @OptIn(ExperimentalMaterial3Api::class)
    fun setBottomSheetState(sheetState: SheetValue) {
        viewModelScope.launch {
            _bottomSheetState.emit(sheetState)
        }
    }
}
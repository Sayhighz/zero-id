package com.zero.id.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.zero.id.app.ui.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _navigateTo = MutableStateFlow<String?>(null)
    val navigateTo = _navigateTo.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    fun onScannedData(scannedData: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val map = Gson().fromJson(scannedData, Map::class.java)
                if (map.containsKey("type") && map["type"] == "IDENTITY_REQUEST") {
                    _navigateTo.value = Screen.ProofGeneration.createRoute(scannedData)
                } else {
                    _toastMessage.value = "Invalid QR Code for Identity Request"
                }
            } catch (e: Exception) {
                _toastMessage.value = "Unknown QR Code Format"
            }
        }
    }

    fun onNavigated() {
        _navigateTo.value = null
    }

    fun onToastShown() {
        _toastMessage.value = null
    }
}

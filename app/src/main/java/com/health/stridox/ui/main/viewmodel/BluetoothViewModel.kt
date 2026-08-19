package com.health.stridox.ui.main.viewmodel

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import com.health.stridox.data.SensorData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothViewModel(application: Application) : AndroidViewModel(application) {

    // UI-safe flows
    private val _sensorData = MutableStateFlow<SensorData?>(null)
    val sensorData: StateFlow<SensorData?> = _sensorData.asStateFlow()

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _foundDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val foundDevices: StateFlow<List<BluetoothDevice>> = _foundDevices.asStateFlow()

    /**
     * Called by the Activity/Service-bridge to update UI state.
     * IMPORTANT: These methods do not hold references to Context / Service.
     */
    fun onSensorData(data: SensorData) {
        _sensorData.value = data
    }

    fun onConnectionChanged(isConnected: Boolean) {
        _connected.value = isConnected
    }

    fun onFoundDevices(devices: List<BluetoothDevice>) {
        _foundDevices.value = devices
    }
}
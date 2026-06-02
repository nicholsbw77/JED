package com.jed.app.ui.home

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jed.app.bluetooth.BluetoothTransport
import com.jed.app.data.db.VehicleDao
import com.jed.app.data.model.Vehicle
import com.jed.app.elm327.ElmConnection
import com.jed.app.obd.ObdService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transport: BluetoothTransport,
    private val elm: ElmConnection,
    private val obdService: ObdService,
    private val vehicleDao: VehicleDao
) : ViewModel() {

    val connectionState = transport.connectionState
    val deviceName = transport.deviceName
    val batteryVoltage = elm.batteryVoltage

    val vehicles = vehicleDao.getAll().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDevice>> = _pairedDevices

    private val _showAddVehicle = MutableStateFlow(false)
    val showAddVehicle: StateFlow<Boolean> = _showAddVehicle

    private val _connectError = MutableStateFlow<String?>(null)
    val connectError: StateFlow<String?> = _connectError

    fun loadPairedDevices() {
        _pairedDevices.value = transport.getPairedDevices()
    }

    @Suppress("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            _connectError.value = null
            val success = transport.connect(device)
            if (success) {
                elm.initialize()
                obdService.detectProtocol()
                val vin = obdService.readVin()
                if (vin != null) {
                    val existing = vehicleDao.getByVin(vin)
                    if (existing != null) {
                        vehicleDao.update(existing.copy(detectedProtocol = obdService.detectedProtocol.value.name))
                    }
                }
            } else {
                _connectError.value = "Failed to connect to ${device.name ?: "device"}"
            }
        }
    }

    fun disconnect() { transport.disconnect() }
    fun showAddVehicle() { _showAddVehicle.value = true }
    fun hideAddVehicle() { _showAddVehicle.value = false }

    fun addVehicle(nickname: String, make: String, model: String, year: Int) {
        viewModelScope.launch {
            vehicleDao.insert(Vehicle(nickname = nickname, make = make, model = model, year = year))
            _showAddVehicle.value = false
        }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch { vehicleDao.delete(vehicle) }
    }
}

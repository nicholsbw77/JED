package com.jed.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jed.app.data.db.VehicleDao
import com.jed.app.data.model.Vehicle
import com.jed.app.elm327.ElmConnection
import com.jed.app.obd.ObdService
import com.jed.app.transport.AdapterTarget
import com.jed.app.transport.AdapterType
import com.jed.app.transport.TransportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transport: TransportManager,
    private val elm: ElmConnection,
    private val obdService: ObdService,
    private val vehicleDao: VehicleDao
) : ViewModel() {

    val connectionState = transport.connectionState
    val deviceName = transport.deviceName
    val adapterType = transport.adapterType
    val batteryVoltage = elm.batteryVoltage

    val vehicles = vehicleDao.getAll().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _selectedAdapterType = MutableStateFlow(AdapterType.BLUETOOTH_CLASSIC)
    val selectedAdapterType: StateFlow<AdapterType> = _selectedAdapterType

    private val _classicAdapters = MutableStateFlow<List<AdapterTarget.Classic>>(emptyList())
    val classicAdapters: StateFlow<List<AdapterTarget.Classic>> = _classicAdapters

    private val _bleAdapters = MutableStateFlow<List<AdapterTarget.Ble>>(emptyList())
    val bleAdapters: StateFlow<List<AdapterTarget.Ble>> = _bleAdapters

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private val _wifiHost = MutableStateFlow(AdapterTarget.DEFAULT_WIFI_HOST)
    val wifiHost: StateFlow<String> = _wifiHost

    private val _wifiPort = MutableStateFlow(AdapterTarget.DEFAULT_WIFI_PORT.toString())
    val wifiPort: StateFlow<String> = _wifiPort

    private val _showAddVehicle = MutableStateFlow(false)
    val showAddVehicle: StateFlow<Boolean> = _showAddVehicle

    private val _showAdapterGuide = MutableStateFlow(false)
    val showAdapterGuide: StateFlow<Boolean> = _showAdapterGuide

    private val _connectError = MutableStateFlow<String?>(null)
    val connectError: StateFlow<String?> = _connectError

    fun selectAdapterType(type: AdapterType) {
        _selectedAdapterType.value = type
        when (type) {
            AdapterType.BLUETOOTH_CLASSIC -> refreshClassicAdapters()
            AdapterType.BLUETOOTH_LE -> scanBleAdapters()
            AdapterType.WIFI -> {}
        }
    }

    fun refreshClassicAdapters() {
        _classicAdapters.value = transport.pairedClassicAdapters()
    }

    fun scanBleAdapters() {
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            _bleAdapters.value = transport.scanBleAdapters()
            _isScanning.value = false
        }
    }

    fun setWifiHost(host: String) { _wifiHost.value = host }
    fun setWifiPort(port: String) { _wifiPort.value = port }

    fun connectWifi() {
        val port = _wifiPort.value.toIntOrNull()
        if (port == null) {
            _connectError.value = "Invalid Wi-Fi port"
            return
        }
        connect(AdapterTarget.Wifi(_wifiHost.value.trim(), port))
    }

    fun connect(target: AdapterTarget) {
        viewModelScope.launch {
            _connectError.value = null
            val success = transport.connect(target)
            if (success) {
                elm.initialize()
                obdService.detectProtocol()
                val vin = obdService.readVin()
                if (vin != null) {
                    val existing = vehicleDao.getByVin(vin)
                    if (existing != null) {
                        vehicleDao.update(
                            existing.copy(detectedProtocol = obdService.detectedProtocol.value.name)
                        )
                    }
                }
            } else {
                _connectError.value = "Failed to connect to ${target.displayName}"
            }
        }
    }

    fun disconnect() { transport.disconnect() }
    fun showAddVehicle() { _showAddVehicle.value = true }
    fun hideAddVehicle() { _showAddVehicle.value = false }
    fun showAdapterGuide() { _showAdapterGuide.value = true }
    fun hideAdapterGuide() { _showAdapterGuide.value = false }

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

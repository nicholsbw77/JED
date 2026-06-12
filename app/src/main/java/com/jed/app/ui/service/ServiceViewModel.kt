package com.jed.app.ui.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jed.app.elm327.ElmConnection
import com.jed.app.obd.ObdService
import com.jed.app.obd.ReadinessResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VehicleInfo(
    val vin: String?,
    val calibrationId: String?,
    val cvn: String?
)

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val obdService: ObdService,
    private val elm: ElmConnection
) : ViewModel() {

    val protocolName: StateFlow<String> = elm.protocolName
    val batteryVoltage: StateFlow<String> = elm.batteryVoltage

    private val _readiness = MutableStateFlow<ReadinessResult?>(null)
    val readiness: StateFlow<ReadinessResult?> = _readiness

    private val _vehicleInfo = MutableStateFlow<VehicleInfo?>(null)
    val vehicleInfo: StateFlow<VehicleInfo?> = _vehicleInfo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    fun refreshReadiness() {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Reading emissions readiness..."
            elm.readBatteryVoltage()
            val result = obdService.readReadiness()
            _readiness.value = result
            _statusMessage.value = if (result == null) {
                "No readiness data (is the vehicle connected and key on?)"
            } else {
                "Readiness updated"
            }
            _isLoading.value = false
        }
    }

    fun refreshVehicleInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Reading vehicle information..."
            val info = VehicleInfo(
                vin = obdService.readVin(),
                calibrationId = obdService.readCalibrationId(),
                cvn = obdService.readCvn()
            )
            _vehicleInfo.value = info
            _statusMessage.value = if (info.vin == null && info.calibrationId == null) {
                "Vehicle info not available on this ECU"
            } else {
                "Vehicle info updated"
            }
            _isLoading.value = false
        }
    }
}

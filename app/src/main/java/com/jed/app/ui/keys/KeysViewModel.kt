package com.jed.app.ui.keys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jed.app.data.db.PinStorageDao
import com.jed.app.data.model.PinRecord
import com.jed.app.protocol.ford.FordPats
import com.jed.app.protocol.gm.GmSecurity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeysViewModel @Inject constructor(
    private val fordPats: FordPats,
    private val gmSecurity: GmSecurity,
    private val pinStorageDao: PinStorageDao
) : ViewModel() {

    private val _make = MutableStateFlow("Ford")
    val make: StateFlow<String> = _make

    private val _pin = MutableStateFlow<String?>(null)
    val pin: StateFlow<String?> = _pin

    private val _keyCount = MutableStateFlow<Int?>(null)
    val keyCount: StateFlow<Int?> = _keyCount

    private val _isWorking = MutableStateFlow(false)
    val isWorking: StateFlow<Boolean> = _isWorking

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _gmWizardStep = MutableStateFlow(0)
    val gmWizardStep: StateFlow<Int> = _gmWizardStep

    private val _gmTimerActive = MutableStateFlow(false)
    val gmTimerActive: StateFlow<Boolean> = _gmTimerActive

    private val _gmSecuritySystem = MutableStateFlow(GmSecurity.SecuritySystem.PASSLOCK)
    val gmSecuritySystem: StateFlow<GmSecurity.SecuritySystem> = _gmSecuritySystem

    val pinHistory = pinStorageDao.getAll()

    fun setMake(make: String) {
        _make.value = make
        _pin.value = null; _keyCount.value = null
        _statusMessage.value = null; _errorMessage.value = null
        _gmWizardStep.value = 0
    }

    fun retrieveFordPin(vehicleId: Long, vin: String) {
        viewModelScope.launch {
            _isWorking.value = true
            _statusMessage.value = "Connecting to PATS module..."
            _errorMessage.value = null
            val result = fordPats.retrievePin()
            if (result.pin != null) {
                _pin.value = result.pin
                _keyCount.value = result.keyCount
                _statusMessage.value = "PIN retrieved successfully"
                pinStorageDao.insert(PinRecord(
                    vehicleId = vehicleId, vin = vin,
                    encryptedPin = result.pin, system = "PATS ${result.generation.name}"
                ))
            } else {
                _errorMessage.value = result.error ?: "Failed to retrieve PIN"
            }
            _isWorking.value = false
        }
    }

    fun setGmSecuritySystem(year: Int, model: String) {
        _gmSecuritySystem.value = GmSecurity.determineSecuritySystem(year, model)
    }

    fun advanceGmWizard() {
        val steps = GmSecurity.getReLearnSteps(_gmSecuritySystem.value)
        if (_gmWizardStep.value < steps.size - 1) {
            _gmWizardStep.value++
            if (steps[_gmWizardStep.value].waitTimeMs > 0) _gmTimerActive.value = true
        }
    }

    fun previousGmWizard() {
        if (_gmWizardStep.value > 0) { _gmWizardStep.value--; _gmTimerActive.value = false }
    }

    fun onTimerComplete() { _gmTimerActive.value = false }
}

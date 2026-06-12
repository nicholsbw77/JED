package com.jed.app.ui.keys

import androidx.lifecycle.ViewModel
import com.jed.app.protocol.gm.GmSecurity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Backs the immobilizer/security guidance screen. Ford PATS PIN extraction and
 * GM "security access" OBD calls were removed — they cannot work over a generic
 * ELM327. What remains is honest guidance plus the owner-performed GM relearn
 * wizard with timed steps.
 */
@HiltViewModel
class KeysViewModel @Inject constructor() : ViewModel() {

    private val _make = MutableStateFlow("Ford")
    val make: StateFlow<String> = _make

    private val _gmWizardStep = MutableStateFlow(0)
    val gmWizardStep: StateFlow<Int> = _gmWizardStep

    private val _gmTimerActive = MutableStateFlow(false)
    val gmTimerActive: StateFlow<Boolean> = _gmTimerActive

    private val _gmSecuritySystem = MutableStateFlow(GmSecurity.SecuritySystem.PASSLOCK)
    val gmSecuritySystem: StateFlow<GmSecurity.SecuritySystem> = _gmSecuritySystem

    fun setMake(make: String) {
        _make.value = make
        _gmWizardStep.value = 0
        _gmTimerActive.value = false
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
        if (_gmWizardStep.value > 0) {
            _gmWizardStep.value--
            _gmTimerActive.value = false
        }
    }

    fun onTimerComplete() { _gmTimerActive.value = false }
}

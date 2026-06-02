package com.jed.app.ui.tune

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jed.app.protocol.ford.FordTuning
import com.jed.app.protocol.ford.TuningParameter
import com.jed.app.protocol.gm.GmTuning
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TuneViewModel @Inject constructor(
    private val fordTuning: FordTuning,
    private val gmTuning: GmTuning
) : ViewModel() {

    private val _make = MutableStateFlow("Ford")
    val make: StateFlow<String> = _make

    private val _parameters = MutableStateFlow<List<TuningParameter>>(emptyList())
    val parameters: StateFlow<List<TuningParameter>> = _parameters

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    private val _showConfirm = MutableStateFlow<TuneAction?>(null)
    val showConfirm: StateFlow<TuneAction?> = _showConfirm

    private val _isWorking = MutableStateFlow(false)
    val isWorking: StateFlow<Boolean> = _isWorking

    data class TuneAction(val name: String, val description: String, val action: suspend () -> Boolean)

    fun setMake(make: String) {
        _make.value = make
        loadParameters()
    }

    fun loadParameters() {
        _parameters.value = when (_make.value) {
            "Ford" -> fordTuning.getAvailableParameters()
            "Chevrolet" -> gmTuning.getAvailableParameters()
            else -> emptyList()
        }
    }

    fun requestAction(action: TuneAction) { _showConfirm.value = action }

    fun confirmAction() {
        val action = _showConfirm.value ?: return
        _showConfirm.value = null
        viewModelScope.launch {
            _isWorking.value = true
            _statusMessage.value = "Running ${action.name}..."
            val success = action.action()
            _statusMessage.value = if (success) "${action.name} completed" else "${action.name} failed"
            _isWorking.value = false
        }
    }

    fun dismissConfirm() { _showConfirm.value = null }

    fun resetFuelTrims() = requestAction(TuneAction(
        "Fuel Trim Reset", "This will reset short and long term fuel trims to zero."
    ) { if (_make.value == "Ford") fordTuning.resetFuelTrims() else gmTuning.resetFuelTrims() })

    fun resetTransmissionAdaptive() = requestAction(TuneAction(
        "Transmission Adaptive Reset", "This will reset transmission shift adaptation. The transmission will relearn shift points."
    ) { if (_make.value == "Ford") fordTuning.resetTransmissionAdaptive() else gmTuning.resetTransmissionAdaptive() })

    fun tpsRelearn() = requestAction(TuneAction(
        "TPS Relearn", "This will relearn the throttle position sensor."
    ) { if (_make.value == "Ford") fordTuning.tpsRelearn() else gmTuning.throttleBodyRelearn() })

    fun idleRelearn() = requestAction(TuneAction(
        "Idle Relearn", "This will reset the idle speed adaptation."
    ) { if (_make.value == "Ford") true else gmTuning.idleRelearn() })
}

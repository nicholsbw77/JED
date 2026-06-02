package com.jed.app.ui.diag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jed.app.data.DtcDatabase
import com.jed.app.data.db.DtcHistoryDao
import com.jed.app.data.model.DtcDefinition
import com.jed.app.obd.ObdService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiagCode(
    val code: String,
    val definition: DtcDefinition?,
    val isPending: Boolean = false
)

@HiltViewModel
class DiagViewModel @Inject constructor(
    private val obdService: ObdService,
    private val dtcDatabase: DtcDatabase,
    private val dtcHistoryDao: DtcHistoryDao
) : ViewModel() {

    private val _codes = MutableStateFlow<List<DiagCode>>(emptyList())
    val codes: StateFlow<List<DiagCode>> = _codes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showClearConfirm = MutableStateFlow(false)
    val showClearConfirm: StateFlow<Boolean> = _showClearConfirm

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    fun readCodes() {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Reading trouble codes..."

            val currentCodes = obdService.readDtcs().map { code ->
                DiagCode(code, dtcDatabase.lookup(code), isPending = false)
            }
            val pendingCodes = obdService.readPendingDtcs().map { code ->
                DiagCode(code, dtcDatabase.lookup(code), isPending = true)
            }

            _codes.value = (currentCodes + pendingCodes).sortedWith(
                compareBy<DiagCode> { it.definition?.severity?.ordinal ?: 1 }
                    .thenBy { it.isPending }
            )

            _statusMessage.value = "${currentCodes.size} active, ${pendingCodes.size} pending"
            _isLoading.value = false
        }
    }

    fun requestClearCodes() {
        _showClearConfirm.value = true
    }

    fun confirmClearCodes(vehicleId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _showClearConfirm.value = false
            val success = obdService.clearDtcs()
            if (success) {
                dtcHistoryDao.markAllCleared(vehicleId)
                _codes.value = emptyList()
                _statusMessage.value = "Codes cleared successfully"
            } else {
                _statusMessage.value = "Failed to clear codes"
            }
            _isLoading.value = false
        }
    }

    fun dismissClearConfirm() {
        _showClearConfirm.value = false
    }
}

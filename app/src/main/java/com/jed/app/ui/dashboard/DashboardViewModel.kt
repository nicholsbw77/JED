package com.jed.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jed.app.transport.ConnectionState
import com.jed.app.transport.TransportManager
import com.jed.app.data.db.DataLogDao
import com.jed.app.data.model.DataLogEntry
import com.jed.app.obd.LivePidReading
import com.jed.app.obd.ObdPid
import com.jed.app.obd.ObdService
import com.jed.app.obd.StandardPids
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val obdService: ObdService,
    private val transport: TransportManager,
    private val dataLogDao: DataLogDao
) : ViewModel() {

    private val _activePids = MutableStateFlow(StandardPids.DASHBOARD_DEFAULTS)
    val activePids: StateFlow<List<ObdPid>> = _activePids

    private val _readings = MutableStateFlow<Map<String, LivePidReading>>(emptyMap())
    val readings: StateFlow<Map<String, LivePidReading>> = _readings

    private val _history = MutableStateFlow<Map<String, List<Float>>>(emptyMap())
    val history: StateFlow<Map<String, List<Float>>> = _history

    private val _isPolling = MutableStateFlow(false)
    val isPolling: StateFlow<Boolean> = _isPolling

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _expandedPid = MutableStateFlow<String?>(null)
    val expandedPid: StateFlow<String?> = _expandedPid

    private var pollingJob: Job? = null
    private var currentSessionId: String? = null
    private var currentVehicleId: Long = 0

    fun startPolling() {
        if (_isPolling.value) return
        _isPolling.value = true

        pollingJob = viewModelScope.launch {
            while (isActive && transport.connectionState.value == ConnectionState.CONNECTED) {
                val newReadings = obdService.readMultiplePids(_activePids.value)
                val readingsMap = _readings.value.toMutableMap()
                val historyMap = _history.value.toMutableMap()

                newReadings.forEach { reading ->
                    readingsMap[reading.pid.code] = reading
                    val pidHistory = historyMap.getOrDefault(reading.pid.code, emptyList()).toMutableList()
                    pidHistory.add(reading.value)
                    if (pidHistory.size > 200) pidHistory.removeAt(0)
                    historyMap[reading.pid.code] = pidHistory
                }

                _readings.value = readingsMap
                _history.value = historyMap

                if (_isRecording.value) {
                    logReadings(newReadings)
                }

                delay(100)
            }
            _isPolling.value = false
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        _isPolling.value = false
    }

    fun toggleRecording(vehicleId: Long) {
        if (_isRecording.value) {
            _isRecording.value = false
            currentSessionId = null
        } else {
            currentVehicleId = vehicleId
            currentSessionId = UUID.randomUUID().toString()
            _isRecording.value = true
        }
    }

    fun expandPid(pidCode: String?) {
        _expandedPid.value = pidCode
    }

    fun addPid(pid: ObdPid) {
        _activePids.value = _activePids.value + pid
    }

    fun removePid(pidCode: String) {
        _activePids.value = _activePids.value.filter { it.code != pidCode }
    }

    private fun logReadings(readings: List<LivePidReading>) {
        val sessionId = currentSessionId ?: return
        viewModelScope.launch {
            val entries = readings.map { reading ->
                DataLogEntry(
                    vehicleId = currentVehicleId,
                    sessionId = sessionId,
                    timestamp = reading.timestamp,
                    pidCode = reading.pid.code,
                    pidName = reading.pid.name,
                    value = reading.value,
                    unit = reading.unit
                )
            }
            dataLogDao.insertAll(entries)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}

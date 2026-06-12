package com.jed.app.transport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single entry point the app uses to talk to whichever adapter is connected.
 * Owns the active [ObdTransport], mirrors its state, and exposes discovery for
 * every supported link type.
 */
@Singleton
class TransportManager @Inject constructor(
    private val classic: BluetoothClassicTransport,
    private val ble: BleTransport,
    private val wifi: WifiTransport
) {
    companion object {
        private const val MAX_RECONNECT_ATTEMPTS = 5
        private const val INITIAL_BACKOFF_MS = 1000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var active: ObdTransport? = null
    private var lastTarget: AdapterTarget? = null
    private var stateJob: Job? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _deviceName = MutableStateFlow("")
    val deviceName: StateFlow<String> = _deviceName

    private val _adapterType = MutableStateFlow<AdapterType?>(null)
    val adapterType: StateFlow<AdapterType?> = _adapterType

    val isBluetoothAvailable: Boolean get() = classic.isBluetoothAvailable

    fun pairedClassicAdapters(): List<AdapterTarget.Classic> = classic.pairedAdapters()

    suspend fun scanBleAdapters(durationMs: Long = 6000): List<AdapterTarget.Ble> =
        ble.scan(durationMs)

    suspend fun connect(target: AdapterTarget): Boolean {
        disconnect()
        val transport: ObdTransport = when (target.type) {
            AdapterType.BLUETOOTH_CLASSIC -> classic
            AdapterType.BLUETOOTH_LE -> ble
            AdapterType.WIFI -> wifi
        }
        active = transport
        lastTarget = target
        _deviceName.value = target.displayName
        _adapterType.value = target.type
        val ok = transport.connect(target)
        // Set the mirrored state synchronously so callers that act on the
        // connect result (e.g. ELM init) see CONNECTED immediately, then keep
        // mirroring later transitions such as drops.
        _connectionState.value = transport.connectionState.value
        stateJob = scope.launch {
            transport.connectionState.collect { _connectionState.value = it }
        }
        if (!ok) {
            _adapterType.value = null
            _deviceName.value = ""
        }
        return ok
    }

    fun disconnect() {
        stateJob?.cancel()
        stateJob = null
        active?.disconnect()
        active = null
        _connectionState.value = ConnectionState.DISCONNECTED
        _deviceName.value = ""
        _adapterType.value = null
    }

    suspend fun send(data: ByteArray) {
        (active ?: throw IOException("No adapter connected")).send(data)
    }

    suspend fun sendString(command: String) {
        (active ?: throw IOException("No adapter connected")).sendString(command)
    }

    suspend fun readUntilPrompt(timeoutMs: Long): String {
        return (active ?: throw IOException("No adapter connected")).readUntilPrompt(timeoutMs)
    }

    fun startAutoReconnect() {
        val target = lastTarget ?: return
        scope.launch {
            var attempt = 0
            while (attempt < MAX_RECONNECT_ATTEMPTS) {
                if (_connectionState.value == ConnectionState.CONNECTED) return@launch
                _connectionState.value = ConnectionState.RECONNECTING
                delay(INITIAL_BACKOFF_MS * (1 shl attempt))
                if (connect(target)) return@launch
                attempt++
            }
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }
}

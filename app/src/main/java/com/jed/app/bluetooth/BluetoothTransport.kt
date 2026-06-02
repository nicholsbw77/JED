package com.jed.app.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class ConnectionState {
    DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING
}

@Singleton
class BluetoothTransport @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val MAX_RECONNECT_ATTEMPTS = 5
        private const val INITIAL_BACKOFF_MS = 1000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private var connectedDevice: BluetoothDevice? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _deviceName = MutableStateFlow("")
    val deviceName: StateFlow<String> = _deviceName

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    @SuppressLint("MissingPermission")
    suspend fun connect(device: BluetoothDevice): Boolean = withContext(Dispatchers.IO) {
        _connectionState.value = ConnectionState.CONNECTING
        try {
            bluetoothAdapter?.cancelDiscovery()
            val btSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            btSocket.connect()
            socket = btSocket
            inputStream = btSocket.inputStream
            outputStream = btSocket.outputStream
            connectedDevice = device
            _deviceName.value = device.name ?: "Unknown"
            _connectionState.value = ConnectionState.CONNECTED
            true
        } catch (e: IOException) {
            _connectionState.value = ConnectionState.DISCONNECTED
            false
        }
    }

    fun disconnect() {
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (_: IOException) {}
        socket = null
        inputStream = null
        outputStream = null
        connectedDevice = null
        _connectionState.value = ConnectionState.DISCONNECTED
        _deviceName.value = ""
    }

    suspend fun send(data: ByteArray) = withContext(Dispatchers.IO) {
        outputStream?.write(data) ?: throw IOException("Not connected")
        outputStream?.flush()
    }

    suspend fun sendString(command: String) {
        send("$command\r".toByteArray(Charsets.US_ASCII))
    }

    suspend fun readUntilPrompt(timeoutMs: Long = 2000): String = withContext(Dispatchers.IO) {
        val buffer = StringBuilder()
        val startTime = System.currentTimeMillis()
        val stream = inputStream ?: throw IOException("Not connected")

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (stream.available() > 0) {
                val byte = stream.read()
                if (byte == -1) throw IOException("Stream closed")
                val char = byte.toChar()
                buffer.append(char)
                if (char == '>') break
            } else {
                delay(10)
            }
        }
        buffer.toString().trim()
    }

    fun startAutoReconnect() {
        scope.launch {
            var attempt = 0
            while (attempt < MAX_RECONNECT_ATTEMPTS) {
                val device = connectedDevice ?: return@launch
                if (_connectionState.value == ConnectionState.CONNECTED) return@launch

                _connectionState.value = ConnectionState.RECONNECTING
                val backoff = INITIAL_BACKOFF_MS * (1 shl attempt)
                delay(backoff)

                if (connect(device)) return@launch
                attempt++
            }
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }
}

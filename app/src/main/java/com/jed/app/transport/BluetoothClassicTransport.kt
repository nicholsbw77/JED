package com.jed.app.transport

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Bluetooth Classic SPP link — the classic ELM327 (e.g. OBDLink MX+, vGate). */
@Singleton
class BluetoothClassicTransport @Inject constructor(
    @ApplicationContext private val context: Context
) : ObdTransport {

    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private var socket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState

    val isBluetoothAvailable: Boolean get() = bluetoothAdapter != null

    @SuppressLint("MissingPermission")
    fun pairedAdapters(): List<AdapterTarget.Classic> {
        return bluetoothAdapter?.bondedDevices
            ?.map { AdapterTarget.Classic(name = it.name ?: "", address = it.address) }
            ?: emptyList()
    }

    @SuppressLint("MissingPermission")
    override suspend fun connect(target: AdapterTarget): Boolean = withContext(Dispatchers.IO) {
        if (target !is AdapterTarget.Classic) return@withContext false
        val adapter = bluetoothAdapter ?: return@withContext false
        _connectionState.value = ConnectionState.CONNECTING
        try {
            adapter.cancelDiscovery()
            val device = adapter.getRemoteDevice(target.address)
            val btSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            btSocket.connect()
            socket = btSocket
            inputStream = btSocket.inputStream
            outputStream = btSocket.outputStream
            _connectionState.value = ConnectionState.CONNECTED
            true
        } catch (e: IOException) {
            disconnect()
            false
        }
    }

    override fun disconnect() {
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (_: IOException) {
        }
        socket = null
        inputStream = null
        outputStream = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    override suspend fun send(data: ByteArray) = withContext(Dispatchers.IO) {
        val out = outputStream ?: throw IOException("Not connected")
        out.write(data)
        out.flush()
    }

    override suspend fun readUntilPrompt(timeoutMs: Long): String = withContext(Dispatchers.IO) {
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
}

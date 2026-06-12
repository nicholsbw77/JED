package com.jed.app.transport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

/** Wi-Fi ELM327 link — a TCP socket to the adapter's access point (e.g. 192.168.0.10:35000). */
@Singleton
class WifiTransport @Inject constructor() : ObdTransport {

    companion object {
        private const val SOCKET_TIMEOUT_MS = 8000
    }

    private var socket: Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState

    override suspend fun connect(target: AdapterTarget): Boolean = withContext(Dispatchers.IO) {
        if (target !is AdapterTarget.Wifi) return@withContext false
        _connectionState.value = ConnectionState.CONNECTING
        try {
            val s = Socket()
            s.connect(InetSocketAddress(target.host, target.port), SOCKET_TIMEOUT_MS)
            s.soTimeout = 0
            socket = s
            inputStream = s.getInputStream()
            outputStream = s.getOutputStream()
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

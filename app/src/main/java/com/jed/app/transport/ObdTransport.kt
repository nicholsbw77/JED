package com.jed.app.transport

import kotlinx.coroutines.flow.StateFlow

/**
 * A byte-stream link to an ELM327-style adapter. Bluetooth Classic, Bluetooth
 * LE, and Wi-Fi each implement this so the rest of the app is link-agnostic.
 */
interface ObdTransport {
    val connectionState: StateFlow<ConnectionState>

    /** Open the link to [target]. Returns true once data can flow. */
    suspend fun connect(target: AdapterTarget): Boolean

    fun disconnect()

    suspend fun send(data: ByteArray)

    /** Read bytes until the ELM327 '>' prompt or [timeoutMs] elapses. */
    suspend fun readUntilPrompt(timeoutMs: Long): String

    suspend fun sendString(command: String) {
        send("$command\r".toByteArray(Charsets.US_ASCII))
    }
}

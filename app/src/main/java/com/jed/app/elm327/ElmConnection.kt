package com.jed.app.elm327

import com.jed.app.bluetooth.BluetoothTransport
import com.jed.app.bluetooth.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElmConnection @Inject constructor(
    private val transport: BluetoothTransport
) {
    private val commandMutex = Mutex()

    private val _initialized = MutableStateFlow(false)
    val initialized: StateFlow<Boolean> = _initialized

    private val _batteryVoltage = MutableStateFlow("--.-V")
    val batteryVoltage: StateFlow<String> = _batteryVoltage

    private val _protocolName = MutableStateFlow("Unknown")
    val protocolName: StateFlow<String> = _protocolName

    suspend fun initialize(): Boolean {
        val initSequence = listOf(
            ElmCommand.RESET,
            ElmCommand.ECHO_OFF,
            ElmCommand.LINEFEED_OFF,
            ElmCommand.SPACES_OFF,
            ElmCommand.HEADERS_ON,
            ElmCommand.AUTO_PROTOCOL
        )

        for (cmd in initSequence) {
            val response = sendCommand(cmd)
            if (response == null && cmd != ElmCommand.RESET) return false
        }

        val voltage = sendCommand(ElmCommand.READ_VOLTAGE)
        if (voltage != null) {
            _batteryVoltage.value = voltage.replace("V", "").trim() + "V"
        }

        val protocol = sendCommand(ElmCommand.READ_PROTOCOL)
        if (protocol != null) {
            _protocolName.value = decodeProtocolNumber(protocol)
        }

        _initialized.value = true
        return true
    }

    suspend fun sendCommand(command: ElmCommand): String? = commandMutex.withLock {
        if (transport.connectionState.value != ConnectionState.CONNECTED) return null
        try {
            transport.sendString(command.command)
            val raw = transport.readUntilPrompt(command.timeoutMs)
            val parsed = parseResponse(raw)
            if (parsed == "?" || parsed.contains("ERROR")) return null
            parsed
        } catch (e: Exception) {
            null
        }
    }

    suspend fun sendObdQuery(pid: String, timeoutMs: Long = 2000): String? {
        return sendCommand(ElmCommand(pid, "OBD $pid", timeoutMs))
    }

    suspend fun readBatteryVoltage(): String? {
        val v = sendCommand(ElmCommand.READ_VOLTAGE)
        if (v != null) _batteryVoltage.value = v.replace("V", "").trim() + "V"
        return v
    }

    private fun decodeProtocolNumber(raw: String): String {
        val num = raw.replace("A", "").trim()
        return when (num) {
            "1" -> "SAE J1850 PWM (Ford)"
            "2" -> "SAE J1850 VPW (GM)"
            "3" -> "ISO 9141-2"
            "4" -> "ISO 14230-4 KWP (5-baud)"
            "5" -> "ISO 14230-4 KWP (fast)"
            "6" -> "ISO 15765-4 CAN (500/11-bit)"
            "7" -> "ISO 15765-4 CAN (500/29-bit)"
            "8" -> "ISO 15765-4 CAN (250/11-bit)"
            "9" -> "ISO 15765-4 CAN (250/29-bit)"
            else -> "Unknown ($raw)"
        }
    }

    companion object {
        fun parseResponse(raw: String): String {
            val cleaned = raw
                .replace(">", "")
                .replace("\r", "\n")
                .trim()

            if (cleaned == "OK" || cleaned == "NO DATA" || cleaned == "?" ||
                cleaned.contains("ERROR") || cleaned.contains("UNABLE") ||
                cleaned.contains("ELM") || cleaned.endsWith("V")) {
                return cleaned
            }

            val lines = cleaned.split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            return lines.joinToString("\n") { line ->
                line.replace(" ", "")
            }
        }

        fun parseHexBytes(hex: String): ByteArray {
            val clean = hex.replace(" ", "")
            return ByteArray(clean.length / 2) { i ->
                clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        }
    }
}

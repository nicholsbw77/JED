package com.jed.app.obd

import com.jed.app.elm327.ElmCommand
import com.jed.app.elm327.ElmConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class LivePidReading(
    val pid: ObdPid,
    val value: Float,
    val unit: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class MonitorStatus(
    val name: String,
    val supported: Boolean,
    val complete: Boolean
)

data class ReadinessResult(
    val milOn: Boolean,
    val dtcCount: Int,
    val monitors: List<MonitorStatus>
)

@Singleton
class ObdService @Inject constructor(
    private val elm: ElmConnection
) {
    private val _detectedProtocol = MutableStateFlow(ObdProtocol.AUTO)
    val detectedProtocol: StateFlow<ObdProtocol> = _detectedProtocol

    private val _supportedPids = MutableStateFlow<Set<String>>(emptySet())
    val supportedPids: StateFlow<Set<String>> = _supportedPids

    suspend fun detectProtocol(): ObdProtocol {
        val response = elm.sendCommand(ElmCommand.READ_PROTOCOL) ?: return ObdProtocol.AUTO
        val protocol = ObdProtocol.fromElmResponse(response)
        _detectedProtocol.value = protocol
        return protocol
    }

    suspend fun querySupportedPids() {
        val supported = mutableSetOf<String>()
        val response = elm.sendObdQuery("0100") ?: return
        val decoded = response.replace(" ", "")
        if (decoded.length >= 12 && decoded.startsWith("4100")) {
            val bitmap = decoded.substring(4).toLongOrNull(16) ?: return
            for (i in 0 until 32) {
                if (bitmap and (1L shl (31 - i)) != 0L) {
                    supported.add(String.format("%02X", i + 1))
                }
            }
        }
        _supportedPids.value = supported
    }

    suspend fun readPid(pid: ObdPid): LivePidReading? {
        val response = elm.sendObdQuery(pid.queryCommand) ?: return null
        val result = PidDecoder.decodeResponse(response) ?: return null
        return LivePidReading(pid, result.second.value, result.second.unit)
    }

    suspend fun readMultiplePids(pids: List<ObdPid>): List<LivePidReading> {
        return pids.mapNotNull { readPid(it) }
    }

    suspend fun readDtcs(): List<String> {
        val response = elm.sendObdQuery("03", 5000) ?: return emptyList()
        return parseDtcResponse(response)
    }

    suspend fun readPendingDtcs(): List<String> {
        val response = elm.sendObdQuery("07", 5000) ?: return emptyList()
        return parseDtcResponse(response)
    }

    suspend fun clearDtcs(): Boolean {
        val response = elm.sendObdQuery("04", 5000)
        return response != null && (response.contains("44") || response.contains("OK"))
    }

    suspend fun readVin(): String? {
        val response = elm.sendObdQuery("0902", 5000) ?: return null
        return parseVinResponse(response)
    }

    suspend fun readCalibrationId(): String? {
        val response = elm.sendObdQuery("0904", 5000) ?: return null
        return parseAsciiInfo(response, "4904")
    }

    suspend fun readCvn(): String? {
        val response = elm.sendObdQuery("0906", 5000) ?: return null
        val hex = response.replace(" ", "").replace("\n", "")
        val start = hex.indexOf("4906")
        if (start < 0) return null
        val data = hex.substring(start + 4).filter { it.isLetterOrDigit() }
        if (data.length < 2) return null
        return data.chunked(8).joinToString(" ").trim().ifBlank { null }
    }

    suspend fun readReadiness(): ReadinessResult? {
        val response = elm.sendObdQuery("0101", 3000) ?: return null
        val hex = response.replace(" ", "").replace("\n", "")
        val idx = hex.indexOf("4101")
        if (idx < 0) return null
        val data = hex.substring(idx + 4)
        if (data.length < 8) return null
        val a = data.substring(0, 2).toInt(16)
        val b = data.substring(2, 4).toInt(16)
        val c = data.substring(4, 6).toInt(16)
        val d = data.substring(6, 8).toInt(16)

        val monitors = mutableListOf<MonitorStatus>()
        // Continuous monitors (byte B): supported = low nibble, complete = high nibble bit clear.
        monitors += MonitorStatus("Misfire", b and 0x01 != 0, b and 0x10 == 0)
        monitors += MonitorStatus("Fuel System", b and 0x02 != 0, b and 0x20 == 0)
        monitors += MonitorStatus("Components", b and 0x04 != 0, b and 0x40 == 0)
        // Non-continuous monitors: supported in C, "not complete" flagged in D.
        val names = listOf(
            "Catalyst", "Heated Catalyst", "Evap System", "Secondary Air",
            "A/C Refrigerant", "O2 Sensor", "O2 Sensor Heater", "EGR System"
        )
        for (i in 0 until 8) {
            monitors += MonitorStatus(
                name = names[i],
                supported = c and (1 shl i) != 0,
                complete = d and (1 shl i) == 0
            )
        }
        return ReadinessResult(milOn = a and 0x80 != 0, dtcCount = a and 0x7F, monitors = monitors)
    }

    private fun parseAsciiInfo(response: String, marker: String): String? {
        val hex = response.replace(" ", "").replace("\n", "")
        val start = hex.indexOf(marker)
        if (start < 0) return null
        val data = hex.substring(start + 4)
        val bytes = ByteArray(data.length / 2) { i ->
            data.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return String(bytes, Charsets.US_ASCII)
            .filter { it.isLetterOrDigit() || it == '.' || it == '-' || it == '/' }
            .trim()
            .ifBlank { null }
    }

    private fun parseDtcResponse(response: String): List<String> {
        val hex = response.replace(" ", "").replace("\n", "")
        val codes = mutableListOf<String>()

        var idx = if (hex.startsWith("43")) 2 else if (hex.startsWith("47")) 2 else 0

        while (idx + 4 <= hex.length) {
            val dtcHex = hex.substring(idx, idx + 4)
            if (dtcHex == "0000") {
                idx += 4
                continue
            }

            val firstByte = dtcHex.substring(0, 1).toInt(16)
            val prefix = when (firstByte shr 2) {
                0 -> "P0"
                1 -> "P1"
                2 -> "P2"
                3 -> "P3"
                4 -> "C0"
                5 -> "C1"
                6 -> "C2"
                7 -> "C3"
                8 -> "B0"
                9 -> "B1"
                10 -> "B2"
                11 -> "B3"
                12 -> "U0"
                13 -> "U1"
                14 -> "U2"
                15 -> "U3"
                else -> "P0"
            }
            val suffix = dtcHex.substring(1)
            codes.add("$prefix$suffix")
            idx += 4
        }
        return codes
    }

    private fun parseVinResponse(response: String): String? {
        val hex = response.replace(" ", "").replace("\n", "")
        val vinStart = hex.indexOf("4902")
        if (vinStart < 0) return null
        val data = hex.substring(vinStart + 4)
        val bytes = ByteArray(data.length / 2) { i ->
            data.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return String(bytes, Charsets.US_ASCII)
            .filter { it.isLetterOrDigit() }
            .take(17)
    }
}

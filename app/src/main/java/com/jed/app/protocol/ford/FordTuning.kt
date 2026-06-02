package com.jed.app.protocol.ford

import com.jed.app.elm327.ElmCommand
import com.jed.app.elm327.ElmConnection
import javax.inject.Inject
import javax.inject.Singleton

data class TuningParameter(
    val name: String,
    val category: String,
    val currentValue: Float?,
    val unit: String,
    val minValue: Float,
    val maxValue: Float,
    val readCommand: String,
    val writeCommand: String
)

@Singleton
class FordTuning @Inject constructor(
    private val elm: ElmConnection
) {
    suspend fun readIdleSpeed(): Float? {
        val response = elm.sendObdQuery("2101", 3000) ?: return null
        val hex = response.replace(" ", "")
        if (hex.length < 8) return null
        return try {
            hex.substring(4, 8).toInt(16).toFloat()
        } catch (_: NumberFormatException) {
            null
        }
    }

    suspend fun resetFuelTrims(): Boolean {
        val response = elm.sendObdQuery("04", 3000)
        return response != null && !response.contains("ERROR")
    }

    suspend fun runKoeoSelfTest(): List<String> {
        elm.sendCommand(ElmCommand.setHeader("7E0"))
        val response = elm.sendObdQuery("2101", 10000) ?: return listOf("Self-test failed to start")
        return listOf("KOEO self-test complete: $response")
    }

    suspend fun runKoerSelfTest(): List<String> {
        elm.sendCommand(ElmCommand.setHeader("7E0"))
        val response = elm.sendObdQuery("2102", 15000) ?: return listOf("Self-test failed to start")
        return listOf("KOER self-test complete: $response")
    }

    suspend fun resetTransmissionAdaptive(): Boolean {
        val response = elm.sendObdQuery("2F0100", 5000)
        return response != null && !response.contains("ERROR")
    }

    suspend fun tpsRelearn(): Boolean {
        val response = elm.sendObdQuery("2F0200", 5000)
        return response != null && !response.contains("ERROR")
    }

    fun getAvailableParameters(): List<TuningParameter> {
        return listOf(
            TuningParameter("Idle Speed", "Engine", null, "RPM", 500f, 1200f, "2101", "3B01"),
            TuningParameter("Timing Offset", "Engine", null, "°", -10f, 10f, "2102", "3B02"),
            TuningParameter("Short Term Fuel Trim", "Fuel", null, "%", -25f, 25f, "0106", ""),
            TuningParameter("Long Term Fuel Trim", "Fuel", null, "%", -25f, 25f, "0107", ""),
        )
    }
}

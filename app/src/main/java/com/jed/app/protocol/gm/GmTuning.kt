package com.jed.app.protocol.gm

import com.jed.app.elm327.ElmConnection
import com.jed.app.protocol.ford.TuningParameter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GmTuning @Inject constructor(
    private val elm: ElmConnection
) {
    suspend fun resetFuelTrims(): Boolean {
        val response = elm.sendObdQuery("04", 3000)
        return response != null && !response.contains("ERROR")
    }

    suspend fun idleRelearn(): Boolean {
        val response = elm.sendObdQuery("2F010000", 5000)
        return response != null && !response.contains("ERROR")
    }

    suspend fun crankPositionVariationRelearn(): List<String> {
        val steps = mutableListOf<String>()
        steps.add("Starting crankshaft position variation relearn...")
        val response = elm.sendObdQuery("2F020000", 10000)
        if (response != null) {
            steps.add("Command accepted. Accelerate to 4000 RPM and release throttle. Repeat 3 times.")
        } else {
            steps.add("Relearn command not accepted. Check engine is running and at operating temp.")
        }
        return steps
    }

    suspend fun resetTransmissionAdaptive(): Boolean {
        val response = elm.sendObdQuery("2F030000", 5000)
        return response != null && !response.contains("ERROR")
    }

    suspend fun throttleBodyRelearn(): Boolean {
        val response = elm.sendObdQuery("2F040000", 5000)
        return response != null && !response.contains("ERROR")
    }

    fun getAvailableParameters(): List<TuningParameter> {
        return listOf(
            TuningParameter("Idle Speed", "Engine", null, "RPM", 500f, 1100f, "2201", ""),
            TuningParameter("Short Term Fuel Trim", "Fuel", null, "%", -25f, 25f, "0106", ""),
            TuningParameter("Long Term Fuel Trim", "Fuel", null, "%", -25f, 25f, "0107", ""),
        )
    }
}

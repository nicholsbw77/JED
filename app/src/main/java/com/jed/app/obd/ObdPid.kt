package com.jed.app.obd

data class ObdPid(
    val code: String,
    val name: String,
    val shortName: String,
    val unit: String,
    val minValue: Float,
    val maxValue: Float,
    val mode: String = "01"
) {
    val queryCommand: String get() = "$mode$code"
}

object StandardPids {
    val SUPPORTED_PIDS = ObdPid("00", "Supported PIDs", "PIDs", "", 0f, 0f)
    val ENGINE_LOAD = ObdPid("04", "Engine Load", "Load", "%", 0f, 100f)
    val COOLANT_TEMP = ObdPid("05", "Coolant Temp", "CLT", "°C", -40f, 215f)
    val SHORT_FUEL_TRIM_1 = ObdPid("06", "Short Fuel Trim B1", "STFT1", "%", -100f, 99.2f)
    val LONG_FUEL_TRIM_1 = ObdPid("07", "Long Fuel Trim B1", "LTFT1", "%", -100f, 99.2f)
    val INTAKE_PRESSURE = ObdPid("0B", "Intake Manifold Pressure", "MAP", "kPa", 0f, 255f)
    val RPM = ObdPid("0C", "Engine RPM", "RPM", "rpm", 0f, 16383.75f)
    val SPEED = ObdPid("0D", "Vehicle Speed", "Speed", "km/h", 0f, 255f)
    val TIMING_ADVANCE = ObdPid("0E", "Timing Advance", "Timing", "°", -64f, 63.5f)
    val INTAKE_TEMP = ObdPid("0F", "Intake Air Temp", "IAT", "°C", -40f, 215f)
    val MAF_RATE = ObdPid("10", "MAF Air Flow", "MAF", "g/s", 0f, 655.35f)
    val THROTTLE = ObdPid("11", "Throttle Position", "TPS", "%", 0f, 100f)
    val ENGINE_RUNTIME = ObdPid("1F", "Engine Run Time", "Runtime", "sec", 0f, 65535f)
    val FUEL_LEVEL = ObdPid("2F", "Fuel Level", "Fuel", "%", 0f, 100f)
    val BARO_PRESSURE = ObdPid("33", "Barometric Pressure", "Baro", "kPa", 0f, 255f)
    val CATALYST_TEMP = ObdPid("3C", "Catalyst Temp B1S1", "CAT", "°C", -40f, 6513.5f)
    val CONTROL_VOLTAGE = ObdPid("42", "Control Module Voltage", "Volts", "V", 0f, 65.535f)
    val ABSOLUTE_LOAD = ObdPid("43", "Absolute Load", "AbsLoad", "%", 0f, 25700f)
    val AMBIENT_TEMP = ObdPid("46", "Ambient Air Temp", "Ambient", "°C", -40f, 215f)
    val OIL_TEMP = ObdPid("5C", "Engine Oil Temp", "Oil", "°C", -40f, 210f)

    val DASHBOARD_DEFAULTS = listOf(RPM, COOLANT_TEMP, SPEED, ENGINE_LOAD, INTAKE_TEMP, SHORT_FUEL_TRIM_1)

    val ALL = listOf(
        ENGINE_LOAD, COOLANT_TEMP, SHORT_FUEL_TRIM_1, LONG_FUEL_TRIM_1,
        INTAKE_PRESSURE, RPM, SPEED, TIMING_ADVANCE, INTAKE_TEMP,
        MAF_RATE, THROTTLE, ENGINE_RUNTIME, FUEL_LEVEL, BARO_PRESSURE,
        CATALYST_TEMP, CONTROL_VOLTAGE, ABSOLUTE_LOAD, AMBIENT_TEMP, OIL_TEMP
    )
}

package com.jed.app.obd

data class DecodedValue(
    val value: Float,
    val unit: String
)

object PidDecoder {
    fun decode(pid: String, data: ByteArray): DecodedValue {
        val a = if (data.isNotEmpty()) data[0].toInt() and 0xFF else 0
        val b = if (data.size > 1) data[1].toInt() and 0xFF else 0

        return when (pid.uppercase()) {
            "04" -> DecodedValue(a * 100f / 255f, "%")
            "05", "0F", "46", "5C" -> DecodedValue(a - 40f, "°C")
            "06", "07", "08", "09" -> DecodedValue((a - 128f) * 100f / 128f, "%")
            "0B", "33" -> DecodedValue(a.toFloat(), "kPa")
            "0C" -> DecodedValue(((a * 256) + b) / 4f, "rpm")
            "0D" -> DecodedValue(a.toFloat(), "km/h")
            "0E" -> DecodedValue(a / 2f - 64f, "°")
            "10" -> DecodedValue(((a * 256) + b) / 100f, "g/s")
            "11", "2F" -> DecodedValue(a * 100f / 255f, "%")
            "1F" -> DecodedValue(((a * 256) + b).toFloat(), "sec")
            "3C", "3D", "3E", "3F" -> DecodedValue(((a * 256) + b) / 10f - 40f, "°C")
            "42" -> DecodedValue(((a * 256) + b) / 1000f, "V")
            "43" -> DecodedValue(((a * 256) + b) * 100f / 255f, "%")
            else -> DecodedValue(a.toFloat(), "raw")
        }
    }

    fun decodeResponse(fullResponse: String): Pair<String, DecodedValue>? {
        val hex = fullResponse.replace(" ", "")
        if (hex.length < 4) return null
        if (!hex.startsWith("41")) return null

        val pid = hex.substring(2, 4)
        val dataHex = hex.substring(4)
        val dataBytes = ByteArray(dataHex.length / 2) { i ->
            dataHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return pid to decode(pid, dataBytes)
    }
}

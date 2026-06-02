package com.jed.app.protocol.ford

import com.jed.app.elm327.ElmCommand
import com.jed.app.elm327.ElmConnection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FordPats @Inject constructor(
    private val elm: ElmConnection
) {
    enum class PatsGeneration { PATS1, PATS2, PATS3 }

    data class PatsResult(
        val pin: String?,
        val keyCount: Int?,
        val generation: PatsGeneration,
        val error: String? = null
    )

    suspend fun switchToMsCan(): Boolean {
        elm.sendCommand(ElmCommand("ATSP6", "Set CAN 500/11"))
        elm.sendCommand(ElmCommand.setHeader(FordPids.PATS_HEADER))
        val test = elm.sendObdQuery("2101", 3000)
        return test != null && !test.contains("NO DATA")
    }

    suspend fun retrievePin(): PatsResult {
        if (!switchToMsCan()) {
            return PatsResult(null, null, PatsGeneration.PATS1, "Could not access PATS module. Verify connection.")
        }

        val secAccess = elm.sendObdQuery("2701", 5000)
        if (secAccess == null) {
            return PatsResult(null, null, PatsGeneration.PATS1, "Security access denied.")
        }

        val seed = secAccess.replace(" ", "")
        if (seed.startsWith("7F")) {
            return PatsResult(null, null, PatsGeneration.PATS1, "PATS module rejected security request: $seed")
        }

        val pinResponse = elm.sendObdQuery("2702", 5000)
        val pin = pinResponse?.let { parsePatsPin(it.replace(" ", "")) }

        val keyCountResp = elm.sendObdQuery("2101", 3000)
        val keyCount = keyCountResp?.let { parseKeyCount(it.replace(" ", "")) }

        restoreHsCan()

        return PatsResult(
            pin = pin,
            keyCount = keyCount,
            generation = PatsGeneration.PATS2,
            error = if (pin == null) "PIN could not be read. Vehicle may require dealer-level access." else null
        )
    }

    suspend fun readKeyCount(): Int? {
        val response = elm.sendObdQuery("2101", 3000) ?: return null
        return parseKeyCount(response.replace(" ", ""))
    }

    private suspend fun restoreHsCan() {
        elm.sendCommand(ElmCommand.setHeader(FordPids.HS_CAN_HEADER))
    }

    companion object {
        fun parsePatsPin(response: String): String? {
            val hex = response.replace(" ", "")
            if (hex.startsWith("7F")) return null
            if (hex.length >= 8 && hex.startsWith("6727")) {
                return hex.substring(4, 8)
            }
            if (hex.length >= 8 && hex.startsWith("6702")) {
                return hex.substring(4, 8)
            }
            return null
        }

        fun parseKeyCount(response: String): Int? {
            val hex = response.replace(" ", "")
            if (hex.length < 8) return null
            return try {
                hex.takeLast(2).toInt(16)
            } catch (_: NumberFormatException) {
                null
            }
        }

        fun determinePatsGeneration(year: Int, model: String): PatsGeneration {
            return when {
                year < 2000 -> PatsGeneration.PATS1
                year < 2006 -> PatsGeneration.PATS2
                else -> PatsGeneration.PATS3
            }
        }
    }
}

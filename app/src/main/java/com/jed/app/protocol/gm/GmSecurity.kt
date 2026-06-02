package com.jed.app.protocol.gm

import com.jed.app.elm327.ElmCommand
import com.jed.app.elm327.ElmConnection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GmSecurity @Inject constructor(
    private val elm: ElmConnection
) {
    enum class SecuritySystem { VATS, PASSLOCK, PK3, PK3_PLUS }

    data class ReLearnStep(
        val stepNumber: Int,
        val instruction: String,
        val waitTimeMs: Long = 0,
        val requiresScanner: Boolean = false
    )

    suspend fun readSecurityStatus(): String? {
        elm.sendCommand(ElmCommand.setHeader(GmPids.BCM_HEADER))
        val response = elm.sendObdQuery("2201", 3000)
        elm.sendCommand(ElmCommand.setHeader(GmPids.ECM_HEADER))
        return response
    }

    suspend fun startTheftRelearn(): Boolean {
        val response = elm.sendObdQuery("2F01FF00", 5000)
        return response != null && !response.contains("ERROR")
    }

    companion object {
        const val PASSLOCK_RELEARN_TIME_MS = 600_000L // 10 minutes

        fun determineSecuritySystem(year: Int, model: String): SecuritySystem {
            return when {
                year < 2000 -> SecuritySystem.VATS
                year < 2006 -> SecuritySystem.PASSLOCK
                year < 2010 -> SecuritySystem.PK3
                else -> SecuritySystem.PK3_PLUS
            }
        }

        fun getReLearnSteps(system: SecuritySystem): List<ReLearnStep> {
            return when (system) {
                SecuritySystem.PASSLOCK -> listOf(
                    ReLearnStep(1, "Turn ignition to ON (do not start engine)"),
                    ReLearnStep(2, "Wait for SECURITY light to turn off (approximately 10 minutes)", PASSLOCK_RELEARN_TIME_MS),
                    ReLearnStep(3, "Turn ignition OFF for 5 seconds", 5_000),
                    ReLearnStep(4, "Repeat steps 1-3 two more times (3 total cycles)"),
                    ReLearnStep(5, "Start the engine. If it starts and runs, the relearn is complete.")
                )
                SecuritySystem.PK3, SecuritySystem.PK3_PLUS -> listOf(
                    ReLearnStep(1, "Insert programmed key and turn to ON"),
                    ReLearnStep(2, "Scanner will send security relearn command", requiresScanner = true),
                    ReLearnStep(3, "Wait for confirmation from BCM", 10_000, true),
                    ReLearnStep(4, "Turn ignition OFF"),
                    ReLearnStep(5, "Wait 10 seconds, then start engine", 10_000)
                )
                SecuritySystem.VATS -> listOf(
                    ReLearnStep(1, "VATS systems require the correct resistor key pellet"),
                    ReLearnStep(2, "Insert key and turn to ON"),
                    ReLearnStep(3, "Wait for SECURITY light to stop flashing (up to 3 minutes)", 180_000),
                    ReLearnStep(4, "Turn OFF, wait 5 seconds", 5_000),
                    ReLearnStep(5, "Start engine")
                )
            }
        }
    }
}

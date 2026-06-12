package com.jed.app.protocol.gm

/**
 * GM theft-deterrent relearn guidance. These are owner-performed timed
 * procedures (Passlock/PK3 "security light" relearns) that need no special
 * scanner command — the app just walks the user through the timing. We removed
 * the earlier OBD "security access" calls because they cannot work over a
 * generic ELM327.
 */
object GmSecurity {
    enum class SecuritySystem { VATS, PASSLOCK, PK3, PK3_PLUS }

    data class ReLearnStep(
        val stepNumber: Int,
        val instruction: String,
        val waitTimeMs: Long = 0,
        val requiresScanner: Boolean = false
    )

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
                ReLearnStep(1, "Insert a programmed key and turn to ON"),
                ReLearnStep(2, "Leave ignition ON and wait for the SECURITY light to stop flashing", 600_000),
                ReLearnStep(3, "Turn ignition OFF"),
                ReLearnStep(4, "Wait 10 seconds, then start the engine", 10_000),
                ReLearnStep(5, "If it starts and runs, the relearn is complete.")
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

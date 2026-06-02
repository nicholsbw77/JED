package com.jed.app.protocol.ford

import com.jed.app.obd.ObdPid

object FordPids {
    val ABS_THROTTLE = ObdPid("49", "Absolute Throttle B", "TPSb", "%", 0f, 100f)
    val APP_D = ObdPid("4A", "Accelerator Pedal D", "APP-D", "%", 0f, 100f)
    val APP_E = ObdPid("4B", "Accelerator Pedal E", "APP-E", "%", 0f, 100f)
    val COMMANDED_THROTTLE = ObdPid("4C", "Commanded Throttle", "CmdThr", "%", 0f, 100f)

    val ENHANCED = listOf(ABS_THROTTLE, APP_D, APP_E, COMMANDED_THROTTLE)

    const val HS_CAN_HEADER = "7E0"
    const val MS_CAN_HEADER = "7E0"
    const val PATS_HEADER = "726"
    const val PATS_RECEIVER = "72E"
}

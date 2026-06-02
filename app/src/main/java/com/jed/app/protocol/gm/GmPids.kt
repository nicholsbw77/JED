package com.jed.app.protocol.gm

import com.jed.app.obd.ObdPid

object GmPids {
    val TRANS_TEMP = ObdPid("01B4", "Transmission Temp", "Trans", "°C", -40f, 215f)
    val KNOCK_RETARD = ObdPid("011A", "Knock Retard", "Knock", "°", 0f, 45f)

    val ECM_HEADER = "7E0"
    val TCM_HEADER = "7E2"
    val BCM_HEADER = "741"
}

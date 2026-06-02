package com.jed.app.elm327

data class ElmCommand(
    val command: String,
    val description: String,
    val timeoutMs: Long = 2000
) {
    companion object {
        val RESET = ElmCommand("ATZ", "Reset adapter", 5000)
        val ECHO_OFF = ElmCommand("ATE0", "Echo off")
        val LINEFEED_OFF = ElmCommand("ATL0", "Linefeeds off")
        val SPACES_OFF = ElmCommand("ATS0", "Spaces off")
        val HEADERS_ON = ElmCommand("ATH1", "Headers on")
        val AUTO_PROTOCOL = ElmCommand("ATSP0", "Auto-detect protocol")
        val READ_VOLTAGE = ElmCommand("ATRV", "Read battery voltage")
        val READ_PROTOCOL = ElmCommand("ATDPN", "Read protocol number")

        fun setProtocol(num: Int) = ElmCommand("ATSP$num", "Set protocol $num")
        fun setHeader(header: String) = ElmCommand("ATSH$header", "Set header $header")
        fun setCanFilter(filter: String) = ElmCommand("ATCF$filter", "Set CAN filter")
        fun setCanMask(mask: String) = ElmCommand("ATCM$mask", "Set CAN mask")

        fun obdQuery(pid: String) = ElmCommand(pid, "OBD query $pid")
    }
}

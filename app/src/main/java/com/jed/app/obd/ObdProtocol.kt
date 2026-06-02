package com.jed.app.obd

enum class ObdProtocol(val code: Int, val displayName: String) {
    AUTO(0, "Auto"),
    J1850_PWM(1, "SAE J1850 PWM (Ford)"),
    J1850_VPW(2, "SAE J1850 VPW (GM)"),
    ISO_9141(3, "ISO 9141-2"),
    KWP_5BAUD(4, "ISO 14230-4 KWP (5-baud)"),
    KWP_FAST(5, "ISO 14230-4 KWP (fast)"),
    CAN_500_11(6, "CAN 500kbps / 11-bit"),
    CAN_500_29(7, "CAN 500kbps / 29-bit"),
    CAN_250_11(8, "CAN 250kbps / 11-bit"),
    CAN_250_29(9, "CAN 250kbps / 29-bit");

    val isCan: Boolean get() = code in 6..9
    val isFord: Boolean get() = this == J1850_PWM || isCan
    val isGm: Boolean get() = this == J1850_VPW || isCan

    companion object {
        fun fromCode(code: Int): ObdProtocol = entries.firstOrNull { it.code == code } ?: AUTO
        fun fromElmResponse(response: String): ObdProtocol {
            val num = response.replace("A", "").trim().toIntOrNull() ?: return AUTO
            return fromCode(num)
        }
    }
}

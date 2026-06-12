package com.jed.app.transport

/**
 * A concrete adapter the user can connect to. Each variant carries exactly the
 * addressing information its transport needs.
 */
sealed interface AdapterTarget {
    val type: AdapterType
    val displayName: String
    val address: String

    data class Classic(val name: String, override val address: String) : AdapterTarget {
        override val type: AdapterType = AdapterType.BLUETOOTH_CLASSIC
        override val displayName: String get() = name.ifBlank { address }
    }

    data class Ble(val name: String, override val address: String) : AdapterTarget {
        override val type: AdapterType = AdapterType.BLUETOOTH_LE
        override val displayName: String get() = name.ifBlank { address }
    }

    data class Wifi(val host: String, val port: Int) : AdapterTarget {
        override val type: AdapterType = AdapterType.WIFI
        override val address: String get() = "$host:$port"
        override val displayName: String get() = "$host:$port"
    }

    companion object {
        /** Factory clones default to the address most WiFi ELM327 adapters use. */
        const val DEFAULT_WIFI_HOST = "192.168.0.10"
        const val DEFAULT_WIFI_PORT = 35000
    }
}

package com.jed.app.transport

/** The physical link an ELM327-style adapter uses to reach the phone. */
enum class AdapterType(val displayName: String, val shortLabel: String) {
    BLUETOOTH_CLASSIC("Bluetooth Classic (SPP)", "BT"),
    BLUETOOTH_LE("Bluetooth LE", "BLE"),
    WIFI("Wi-Fi", "WiFi")
}

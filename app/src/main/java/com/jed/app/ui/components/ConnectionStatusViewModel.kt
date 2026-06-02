package com.jed.app.ui.components

import androidx.lifecycle.ViewModel
import com.jed.app.bluetooth.BluetoothTransport
import com.jed.app.elm327.ElmConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConnectionStatusViewModel @Inject constructor(
    transport: BluetoothTransport,
    elmConnection: ElmConnection
) : ViewModel() {
    val connectionState = transport.connectionState
    val deviceName = transport.deviceName
    val batteryVoltage = elmConnection.batteryVoltage
}

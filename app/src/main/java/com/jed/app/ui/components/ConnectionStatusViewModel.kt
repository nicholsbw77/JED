package com.jed.app.ui.components

import androidx.lifecycle.ViewModel
import com.jed.app.transport.TransportManager
import com.jed.app.elm327.ElmConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConnectionStatusViewModel @Inject constructor(
    transport: TransportManager,
    elmConnection: ElmConnection
) : ViewModel() {
    val connectionState = transport.connectionState
    val deviceName = transport.deviceName
    val adapterType = transport.adapterType
    val batteryVoltage = elmConnection.batteryVoltage
}

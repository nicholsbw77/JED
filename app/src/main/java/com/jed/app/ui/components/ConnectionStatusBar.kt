package com.jed.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jed.app.ui.theme.DarkGray
import com.jed.app.ui.theme.GreenGood
import com.jed.app.ui.theme.OrangeAccent
import com.jed.app.ui.theme.OffWhite
import com.jed.app.ui.theme.RedAlert
import com.jed.app.ui.theme.YellowWarn
import com.jed.app.bluetooth.ConnectionState

@Composable
fun ConnectionStatusBar(
    viewModel: ConnectionStatusViewModel = hiltViewModel()
) {
    val state by viewModel.connectionState.collectAsState()
    val deviceName by viewModel.deviceName.collectAsState()
    val voltage by viewModel.batteryVoltage.collectAsState()

    val (icon, tint, label) = when (state) {
        ConnectionState.CONNECTED -> Triple(Icons.Default.BluetoothConnected, GreenGood, "Connected")
        ConnectionState.CONNECTING -> Triple(Icons.Default.BluetoothSearching, YellowWarn, "Connecting...")
        ConnectionState.RECONNECTING -> Triple(Icons.Default.BluetoothSearching, OrangeAccent, "Reconnecting...")
        ConnectionState.DISCONNECTED -> Triple(Icons.Default.BluetoothDisabled, RedAlert, "Disconnected")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkGray)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = "Bluetooth status", tint = tint, modifier = Modifier.size(20.dp))
            Text(label, color = tint, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }
        Text(
            text = if (state == ConnectionState.CONNECTED) deviceName else "No Vehicle",
            color = OffWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold
        )
        Text(voltage, color = OrangeAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

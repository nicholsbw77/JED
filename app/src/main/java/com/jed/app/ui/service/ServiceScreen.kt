package com.jed.app.ui.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.jed.app.obd.MonitorStatus
import com.jed.app.ui.theme.CardBackground
import com.jed.app.ui.theme.Charcoal
import com.jed.app.ui.theme.GreenGood
import com.jed.app.ui.theme.LightGray
import com.jed.app.ui.theme.MediumGray
import com.jed.app.ui.theme.OrangeAccent
import com.jed.app.ui.theme.OffWhite
import com.jed.app.ui.theme.RedAlert
import com.jed.app.ui.theme.YellowWarn

@Composable
fun ServiceScreen(viewModel: ServiceViewModel = hiltViewModel()) {
    val protocol by viewModel.protocolName.collectAsState()
    val voltage by viewModel.batteryVoltage.collectAsState()
    val readiness by viewModel.readiness.collectAsState()
    val vehicleInfo by viewModel.vehicleInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(Charcoal)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("SERVICE", color = OrangeAccent, fontSize = 18.sp, fontWeight = FontWeight.Black)
        statusMessage?.let { Text(it, color = LightGray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) }
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoTile("Protocol", protocol, Modifier.weight(1f))
            InfoTile("Battery", voltage, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))
        if (isLoading) {
            CircularProgressIndicator(color = OrangeAccent, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = { viewModel.refreshReadiness() },
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            modifier = Modifier.fillMaxWidth().height(52.dp), enabled = !isLoading
        ) { Text("Read Emissions Readiness", fontWeight = FontWeight.Bold) }

        readiness?.let { result ->
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val milColor = if (result.milOn) RedAlert else GreenGood
                InfoTile("Check Engine", if (result.milOn) "ON" else "OFF", Modifier.weight(1f), milColor)
                InfoTile("Stored DTCs", result.dtcCount.toString(), Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            result.monitors.filter { it.supported }.forEach { MonitorRow(it) }
            if (result.monitors.none { it.supported }) {
                Text("No monitors reported as supported.", color = LightGray, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.refreshVehicleInfo() },
            colors = ButtonDefaults.buttonColors(containerColor = MediumGray),
            modifier = Modifier.fillMaxWidth().height(52.dp), enabled = !isLoading
        ) { Text("Read Vehicle Info", fontWeight = FontWeight.Bold) }

        vehicleInfo?.let { info ->
            Spacer(Modifier.height(12.dp))
            InfoRow("VIN", info.vin ?: "—")
            InfoRow("Calibration ID", info.calibrationId ?: "—")
            InfoRow("CVN", info.cvn ?: "—")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun InfoTile(label: String, value: String, modifier: Modifier = Modifier, valueColor: androidx.compose.ui.graphics.Color = OffWhite) {
    Column(
        modifier = modifier
            .background(CardBackground, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Text(label, color = LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(value, color = valueColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MonitorRow(monitor: MonitorStatus) {
    val statusColor = if (monitor.complete) GreenGood else YellowWarn
    val statusText = if (monitor.complete) "READY" else "NOT READY"
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(monitor.name, color = OffWhite, fontSize = 14.sp)
        Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = LightGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(value, color = OffWhite, fontSize = 14.sp)
    }
}

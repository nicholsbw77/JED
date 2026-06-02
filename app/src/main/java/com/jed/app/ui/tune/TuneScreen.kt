package com.jed.app.ui.tune

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.jed.app.ui.components.ConfirmDialog
import com.jed.app.ui.theme.CardBackground
import com.jed.app.ui.theme.Charcoal
import com.jed.app.ui.theme.LightGray
import com.jed.app.ui.theme.MediumGray
import com.jed.app.ui.theme.OrangeAccent
import com.jed.app.ui.theme.OffWhite

@Composable
fun TuneScreen(viewModel: TuneViewModel = hiltViewModel()) {
    val make by viewModel.make.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val showConfirm by viewModel.showConfirm.collectAsState()
    val isWorking by viewModel.isWorking.collectAsState()

    showConfirm?.let { action ->
        ConfirmDialog(
            title = action.name,
            message = action.description + "\n\nThis changes how your engine runs. Continue?",
            confirmText = "Proceed",
            onConfirm = { viewModel.confirmAction() },
            onDismiss = { viewModel.dismissConfirm() }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Charcoal)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("TUNING", color = OrangeAccent, fontSize = 18.sp, fontWeight = FontWeight.Black)
        statusMessage?.let { Text(it, color = LightGray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) }
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Ford", "Chevrolet").forEach { m ->
                FilterChip(
                    selected = make == m, onClick = { viewModel.setMake(m) },
                    label = { Text(m, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeAccent, containerColor = MediumGray
                    )
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        if (isWorking) {
            CircularProgressIndicator(color = OrangeAccent, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
        }

        SectionHeader("Engine")
        TuneButton("Fuel Trim Reset", "Reset short & long term fuel trims") { viewModel.resetFuelTrims() }
        TuneButton("TPS / Throttle Relearn", "Relearn throttle position") { viewModel.tpsRelearn() }
        TuneButton("Idle Relearn", "Reset idle speed adaptation") { viewModel.idleRelearn() }

        Spacer(Modifier.height(16.dp))
        SectionHeader("Transmission")
        TuneButton("Adaptive Shift Reset", "Reset shift point adaptation") { viewModel.resetTransmissionAdaptive() }

        if (make == "Ford") {
            Spacer(Modifier.height(16.dp))
            SectionHeader("Self-Tests")
            TuneButton("KOEO Self-Test", "Key On Engine Off diagnostic test") { }
            TuneButton("KOER Self-Test", "Key On Engine Running diagnostic test") { }
        }
        if (make == "Chevrolet") {
            Spacer(Modifier.height(16.dp))
            SectionHeader("Relearns")
            TuneButton("Crank Position Relearn", "Crankshaft position variation relearn") { }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, color = OrangeAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun TuneButton(title: String, description: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(64.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(title, color = OffWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(description, color = LightGray, fontSize = 12.sp)
        }
    }
}

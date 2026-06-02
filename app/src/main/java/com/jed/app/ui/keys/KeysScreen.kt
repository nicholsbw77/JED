package com.jed.app.ui.keys

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jed.app.protocol.gm.GmSecurity
import com.jed.app.ui.components.StepWizard
import com.jed.app.ui.components.TimerDisplay
import com.jed.app.ui.components.WizardStep
import com.jed.app.ui.theme.CardBackground
import com.jed.app.ui.theme.Charcoal
import com.jed.app.ui.theme.GreenGood
import com.jed.app.ui.theme.LightGray
import com.jed.app.ui.theme.MediumGray
import com.jed.app.ui.theme.OrangeAccent
import com.jed.app.ui.theme.OffWhite
import com.jed.app.ui.theme.RedAlert

@Composable
fun KeysScreen(viewModel: KeysViewModel = hiltViewModel()) {
    val make by viewModel.make.collectAsState()
    val pin by viewModel.pin.collectAsState()
    val keyCount by viewModel.keyCount.collectAsState()
    val isWorking by viewModel.isWorking.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val gmWizardStep by viewModel.gmWizardStep.collectAsState()
    val gmTimerActive by viewModel.gmTimerActive.collectAsState()
    val gmSecuritySystem by viewModel.gmSecuritySystem.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(Charcoal)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("KEY PROGRAMMING", color = OrangeAccent, fontSize = 18.sp, fontWeight = FontWeight.Black)
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

        if (make == "Ford") {
            FordPatsSection(viewModel, pin, keyCount, isWorking, statusMessage, errorMessage)
        } else {
            GmSecuritySection(viewModel, gmWizardStep, gmTimerActive, gmSecuritySystem)
        }
    }
}

@Composable
private fun FordPatsSection(
    viewModel: KeysViewModel, pin: String?, keyCount: Int?,
    isWorking: Boolean, statusMessage: String?, errorMessage: String?
) {
    val clipboard = LocalClipboardManager.current
    Column {
        Text("Ford PATS PIN Retrieval", color = OffWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Retrieve the security PIN from your Ford's PATS module", color = LightGray, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.retrieveFordPin(0, "") },
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            modifier = Modifier.fillMaxWidth().height(56.dp), enabled = !isWorking
        ) {
            if (isWorking) CircularProgressIndicator(color = OffWhite, modifier = Modifier.padding(end = 8.dp))
            else Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Retrieve PIN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        statusMessage?.let { Text(it, color = GreenGood, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp)) }
        errorMessage?.let { Text(it, color = RedAlert, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp)) }

        if (pin != null) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(CardBackground, RoundedCornerShape(12.dp))
                    .border(2.dp, GreenGood, RoundedCornerShape(12.dp)).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SECURITY PIN", color = GreenGood, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(pin, color = OffWhite, fontSize = 48.sp, fontWeight = FontWeight.Black)
                        IconButton(onClick = { clipboard.setText(AnnotatedString(pin)) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = OrangeAccent)
                        }
                    }
                    keyCount?.let { Text("$it key(s) currently programmed", color = LightGray, fontSize = 13.sp) }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Key Programming Steps", color = OrangeAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            listOf(
                "Insert first programmed key, turn to ON for 1 second, then OFF",
                "Within 5 seconds, insert second programmed key, turn to ON for 1 second, then OFF",
                "Within 10 seconds, insert NEW key, turn to ON",
                "Wait for security light to illuminate for 3 seconds, then turn off",
                "New key is programmed. Turn OFF and test."
            ).forEachIndexed { idx, step ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("${idx + 1}.", color = OrangeAccent, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                    Text(step, color = OffWhite, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun GmSecuritySection(
    viewModel: KeysViewModel, currentStep: Int,
    timerActive: Boolean, securitySystem: GmSecurity.SecuritySystem
) {
    val reLearnSteps = GmSecurity.getReLearnSteps(securitySystem)
    Column {
        Text("GM Security Relearn", color = OffWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("GM vehicles use relearn procedures instead of PINs. System: ${securitySystem.name}",
            color = LightGray, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        val wizardSteps = reLearnSteps.mapIndexed { idx, step ->
            WizardStep(title = "Step ${step.stepNumber}", description = step.instruction,
                isComplete = idx < currentStep, isCurrent = idx == currentStep)
        }
        StepWizard(
            steps = wizardSteps, currentStep = currentStep,
            onNext = { viewModel.advanceGmWizard() },
            onBack = { viewModel.previousGmWizard() },
            nextEnabled = !timerActive,
            nextLabel = if (currentStep >= reLearnSteps.size - 1) "Done" else "Next"
        ) {
            if (timerActive) {
                TimerDisplay(
                    totalMs = reLearnSteps[currentStep].waitTimeMs,
                    label = "Waiting...",
                    onComplete = { viewModel.onTimerComplete() }
                )
            }
        }
    }
}

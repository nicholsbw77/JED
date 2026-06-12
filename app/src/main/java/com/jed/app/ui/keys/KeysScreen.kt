package com.jed.app.ui.keys

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.jed.app.ui.theme.LightGray
import com.jed.app.ui.theme.MediumGray
import com.jed.app.ui.theme.OrangeAccent
import com.jed.app.ui.theme.OffWhite
import com.jed.app.ui.theme.YellowWarn

@Composable
fun KeysScreen(viewModel: KeysViewModel = hiltViewModel()) {
    val make by viewModel.make.collectAsState()
    val gmWizardStep by viewModel.gmWizardStep.collectAsState()
    val gmTimerActive by viewModel.gmTimerActive.collectAsState()
    val gmSecuritySystem by viewModel.gmSecuritySystem.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(Charcoal)
            .verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("SECURITY & KEYS", color = OrangeAccent, fontSize = 18.sp, fontWeight = FontWeight.Black)
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
            FordKeyGuidance()
        } else {
            GmSecuritySection(viewModel, gmWizardStep, gmTimerActive, gmSecuritySystem)
        }
    }
}

@Composable
private fun FordKeyGuidance() {
    Column {
        Text("Ford PATS Keys", color = OffWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(10.dp))
                .border(1.dp, YellowWarn.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Text("PIN extraction is not possible over a generic adapter", color = YellowWarn, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "Reading a Ford PATS incode/outcode PIN requires dealer software (Ford IDS/FDRS) " +
                    "or a dedicated locksmith key-programming tool. An ELM327-class adapter cannot do it, " +
                    "so this app does not pretend to.",
                color = OffWhite.copy(alpha = 0.85f), fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(20.dp))
        Text("Onboard spare-key programming", color = OrangeAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(
            "Many 1998-2011 Fords can add a key WITHOUT any tool if you already have two " +
                "programmed keys. If you have fewer than two keys, you need a locksmith or dealer.",
            color = LightGray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 6.dp)
        )
        listOf(
            "Insert the 1st programmed key, turn to ON for ~3 seconds, then OFF and remove",
            "Within 10 seconds, insert the 2nd programmed key, turn to ON for ~3 seconds, then OFF and remove",
            "Within 20 seconds, insert the NEW (unprogrammed) key and turn to ON",
            "The theft/security light should illuminate for ~3 seconds, then turn off",
            "Turn OFF. The new key should now start the vehicle — test it"
        ).forEachIndexed { idx, step ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Text("${idx + 1}.", color = OrangeAccent, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                Text(step, color = OffWhite, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Exact timing varies by model and year — check your owner's manual to confirm your vehicle supports onboard programming.",
            color = LightGray, fontSize = 12.sp
        )
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
        Text(
            "GM vehicles use owner-performed relearn procedures instead of PINs. System: ${securitySystem.name}",
            color = LightGray, fontSize = 13.sp
        )
        Spacer(Modifier.height(16.dp))

        val wizardSteps = reLearnSteps.mapIndexed { idx, step ->
            WizardStep(
                title = "Step ${step.stepNumber}", description = step.instruction,
                isComplete = idx < currentStep, isCurrent = idx == currentStep
            )
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

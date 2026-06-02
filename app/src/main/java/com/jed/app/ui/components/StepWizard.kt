package com.jed.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jed.app.ui.theme.CardBackground
import com.jed.app.ui.theme.DarkGray
import com.jed.app.ui.theme.GreenGood
import com.jed.app.ui.theme.MediumGray
import com.jed.app.ui.theme.OrangeAccent
import com.jed.app.ui.theme.OffWhite

data class WizardStep(
    val title: String,
    val description: String,
    val isComplete: Boolean = false,
    val isCurrent: Boolean = false
)

@Composable
fun StepWizard(
    steps: List<WizardStep>,
    currentStep: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    nextEnabled: Boolean = true,
    nextLabel: String = "Next",
    content: @Composable () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        steps.forEachIndexed { index, step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (index == currentStep) CardBackground else DarkGray,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        if (index == currentStep) 2.dp else 0.dp,
                        if (index == currentStep) OrangeAccent else MediumGray,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(36.dp).background(
                        when {
                            step.isComplete -> GreenGood
                            index == currentStep -> OrangeAccent
                            else -> MediumGray
                        }, CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (step.isComplete) "✓" else "${index + 1}",
                        color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(step.title, color = OffWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(step.description, color = OffWhite.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(16.dp))
        content()
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            if (currentStep > 0) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = MediumGray),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) { Text("Back", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(12.dp))
            }
            Button(
                onClick = onNext, enabled = nextEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                modifier = Modifier.weight(1f).height(56.dp)
            ) { Text(nextLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

package com.jed.app.ui.diag

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jed.app.data.model.DtcSeverity
import com.jed.app.ui.components.ConfirmDialog
import com.jed.app.ui.theme.CardBackground
import com.jed.app.ui.theme.Charcoal
import com.jed.app.ui.theme.LightGray
import com.jed.app.ui.theme.OrangeAccent
import com.jed.app.ui.theme.OffWhite
import com.jed.app.ui.theme.RedAlert
import com.jed.app.ui.theme.YellowWarn

@Composable
fun DiagScreen(viewModel: DiagViewModel = hiltViewModel()) {
    val codes by viewModel.codes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showClearConfirm by viewModel.showClearConfirm.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    if (showClearConfirm) {
        ConfirmDialog(
            title = "Clear All Codes",
            message = "This will clear all diagnostic trouble codes and turn off the Check Engine Light. Are you sure?",
            confirmText = "Clear Codes",
            isDestructive = true,
            onConfirm = { viewModel.confirmClearCodes(0) },
            onDismiss = { viewModel.dismissClearConfirm() }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Charcoal).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("DIAGNOSTICS", color = OrangeAccent, fontSize = 18.sp, fontWeight = FontWeight.Black)
            statusMessage?.let { Text(it, color = LightGray, fontSize = 12.sp) }
        }
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { viewModel.readCodes() },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Read Codes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { viewModel.requestClearCodes() },
                colors = ButtonDefaults.buttonColors(containerColor = RedAlert),
                modifier = Modifier.weight(1f).height(56.dp),
                enabled = !isLoading && codes.isNotEmpty()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Clear Codes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(16.dp))
        if (isLoading) {
            CircularProgressIndicator(color = OrangeAccent, modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(codes) { diagCode -> DtcCard(diagCode) }
        }
    }
}

@Composable
private fun DtcCard(diagCode: DiagCode) {
    var expanded by remember { mutableStateOf(false) }
    val severityColor = when (diagCode.definition?.severity) {
        DtcSeverity.CRITICAL -> RedAlert
        DtcSeverity.WARNING -> YellowWarn
        DtcSeverity.INFO -> LightGray
        null -> LightGray
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(8.dp))
            .border(1.dp, severityColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(diagCode.code, color = severityColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
                if (diagCode.isPending) {
                    Text(" PENDING", color = YellowWarn, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                diagCode.definition?.severity?.label ?: "Unknown",
                color = severityColor, fontSize = 12.sp, fontWeight = FontWeight.Bold
            )
        }
        Text(
            diagCode.definition?.shortDescription ?: "Unknown code",
            color = OffWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
        )
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                diagCode.definition?.let { def ->
                    Text(def.description, color = OffWhite.copy(alpha = 0.8f), fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Common Causes:", color = OrangeAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    def.commonCauses.forEach { cause ->
                        Text("  • $cause", color = OffWhite.copy(alpha = 0.7f), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

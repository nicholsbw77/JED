package com.jed.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.jed.app.ui.components.GaugeView
import com.jed.app.ui.components.LineGraph
import com.jed.app.ui.theme.Charcoal
import com.jed.app.ui.theme.GreenGood
import com.jed.app.ui.theme.OffWhite
import com.jed.app.ui.theme.OrangeAccent
import com.jed.app.ui.theme.RedAlert

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val activePids by viewModel.activePids.collectAsState()
    val readings by viewModel.readings.collectAsState()
    val history by viewModel.history.collectAsState()
    val isPolling by viewModel.isPolling.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val expandedPid by viewModel.expandedPid.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Charcoal)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("LIVE DATA", color = OrangeAccent, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Row {
                IconButton(onClick = {
                    if (isPolling) viewModel.stopPolling() else viewModel.startPolling()
                }) {
                    Icon(
                        if (isPolling) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPolling) "Stop" else "Start",
                        tint = if (isPolling) RedAlert else GreenGood,
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(onClick = { viewModel.toggleRecording(0) }) {
                    Icon(
                        Icons.Default.FiberManualRecord,
                        contentDescription = "Record",
                        tint = if (isRecording) RedAlert else OffWhite.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (expandedPid != null) {
            val pid = activePids.find { it.code == expandedPid }
            val reading = readings[expandedPid]
            val points = history[expandedPid] ?: emptyList()

            if (pid != null) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(pid.name, color = OffWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewModel.expandPid(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = OffWhite)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    GaugeView(
                        label = pid.shortName,
                        value = reading?.value ?: 0f,
                        unit = pid.unit,
                        minValue = pid.minValue,
                        maxValue = pid.maxValue,
                        modifier = Modifier.size(250.dp).align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(16.dp))
                    LineGraph(
                        label = pid.shortName,
                        dataPoints = points,
                        unit = pid.unit,
                        minValue = pid.minValue,
                        maxValue = pid.maxValue,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(activePids) { pid ->
                    val reading = readings[pid.code]
                    GaugeView(
                        label = pid.shortName,
                        value = reading?.value ?: 0f,
                        unit = pid.unit,
                        minValue = pid.minValue,
                        maxValue = pid.maxValue,
                        modifier = Modifier.padding(4.dp),
                        onClick = { viewModel.expandPid(pid.code) }
                    )
                }
            }
        }
    }
}

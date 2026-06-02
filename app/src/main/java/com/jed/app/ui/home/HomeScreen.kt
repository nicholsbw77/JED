@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.jed.app.ui.home

import android.annotation.SuppressLint
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.jed.app.bluetooth.ConnectionState
import com.jed.app.data.model.Vehicle
import com.jed.app.ui.theme.CardBackground
import com.jed.app.ui.theme.Charcoal
import com.jed.app.ui.theme.GreenGood
import com.jed.app.ui.theme.LightGray
import com.jed.app.ui.theme.MediumGray
import com.jed.app.ui.theme.OrangeAccent
import com.jed.app.ui.theme.OffWhite
import com.jed.app.ui.theme.RedAlert

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDashboard: () -> Unit = {}
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val pairedDevices by viewModel.pairedDevices.collectAsState()
    val showAddVehicle by viewModel.showAddVehicle.collectAsState()
    val connectError by viewModel.connectError.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadPairedDevices() }

    if (showAddVehicle) {
        AddVehicleDialog(
            onDismiss = { viewModel.hideAddVehicle() },
            onAdd = { n, mk, md, y -> viewModel.addVehicle(n, mk, md, y) }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Charcoal).padding(16.dp)) {
        Text("JED", color = OrangeAccent, fontSize = 32.sp, fontWeight = FontWeight.Black)
        Text("OBDx Pro VX Diagnostic Tool", color = LightGray, fontSize = 14.sp)
        Spacer(Modifier.height(24.dp))

        if (connectionState == ConnectionState.DISCONNECTED) {
            Text("CONNECT", color = OffWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            connectError?.let { Text(it, color = RedAlert, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp)) }
            if (pairedDevices.isEmpty()) {
                Text("No paired Bluetooth devices found. Pair your OBDx Pro VX in Android Settings first.",
                    color = LightGray, fontSize = 13.sp)
            }
            pairedDevices.forEach { device ->
                Button(
                    onClick = { viewModel.connectToDevice(device) },
                    colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(56.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Bluetooth, contentDescription = null, tint = OrangeAccent)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                        Text(device.name ?: "Unknown", color = OffWhite, fontWeight = FontWeight.Bold)
                        Text(device.address, color = LightGray, fontSize = 11.sp)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(GreenGood.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .border(1.dp, GreenGood, RoundedCornerShape(8.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Bluetooth, contentDescription = null, tint = GreenGood)
                Spacer(Modifier.width(8.dp))
                Text("Connected", color = GreenGood, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Button(onClick = { viewModel.disconnect() },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAlert)
                ) { Text("Disconnect", fontWeight = FontWeight.Bold) }
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("VEHICLES", color = OffWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            FloatingActionButton(onClick = { viewModel.showAddVehicle() },
                containerColor = OrangeAccent, modifier = Modifier.size(40.dp)
            ) { Icon(Icons.Default.Add, contentDescription = "Add vehicle", tint = Charcoal) }
        }
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(vehicles) { vehicle -> VehicleCard(vehicle, onClick = { onNavigateToDashboard() }) }
        }
    }
}

@Composable
private fun VehicleCard(vehicle: Vehicle, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(12.dp))
            .border(1.dp, MediumGray, RoundedCornerShape(12.dp))
            .clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(40.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(vehicle.nickname, color = OffWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("${vehicle.year} ${vehicle.make} ${vehicle.model}", color = LightGray, fontSize = 14.sp)
            if (vehicle.vin.isNotEmpty()) Text("VIN: ${vehicle.vin}", color = LightGray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun AddVehicleDialog(onDismiss: () -> Unit, onAdd: (String, String, String, Int) -> Unit) {
    var nickname by remember { mutableStateOf("") }
    var make by remember { mutableStateOf("Ford") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableIntStateOf(2005) }
    var makeExpanded by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = OrangeAccent, unfocusedBorderColor = MediumGray,
        focusedTextColor = OffWhite, unfocusedTextColor = OffWhite,
        cursorColor = OrangeAccent, focusedLabelColor = OrangeAccent, unfocusedLabelColor = LightGray
    )

    Dialog(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.background(CardBackground, RoundedCornerShape(12.dp)).padding(24.dp)) {
            Text("Add Vehicle", color = OffWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = nickname, onValueChange = { nickname = it },
                label = { Text("Nickname") }, colors = textFieldColors, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(expanded = makeExpanded, onExpandedChange = { makeExpanded = it }) {
                OutlinedTextField(value = make, onValueChange = {}, readOnly = true,
                    label = { Text("Make") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = makeExpanded) },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable))
                ExposedDropdownMenu(expanded = makeExpanded, onDismissRequest = { makeExpanded = false }) {
                    listOf("Ford", "Chevrolet").forEach { m ->
                        DropdownMenuItem(text = { Text(m) }, onClick = { make = m; makeExpanded = false })
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = model, onValueChange = { model = it },
                label = { Text("Model") }, colors = textFieldColors, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = year.toString(), onValueChange = { it.toIntOrNull()?.let { y -> year = y } },
                label = { Text("Year") }, colors = textFieldColors, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = MediumGray)) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onAdd(nickname, make, model, year) },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    enabled = nickname.isNotBlank() && model.isNotBlank()
                ) { Text("Add", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

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
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.jed.app.data.VehicleCatalog
import com.jed.app.data.model.Vehicle
import com.jed.app.transport.AdapterType
import com.jed.app.transport.ConnectionState
import com.jed.app.ui.adapter.AdapterGuideScreen
import com.jed.app.ui.theme.CardBackground
import com.jed.app.ui.theme.Charcoal
import com.jed.app.ui.theme.GreenGood
import com.jed.app.ui.theme.LightGray
import com.jed.app.ui.theme.MediumGray
import com.jed.app.ui.theme.OrangeAccent
import com.jed.app.ui.theme.OffWhite
import com.jed.app.ui.theme.RedAlert
import com.jed.app.ui.theme.YellowWarn

@SuppressLint("MissingPermission")
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDashboard: () -> Unit = {}
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val adapterType by viewModel.adapterType.collectAsState()
    val deviceName by viewModel.deviceName.collectAsState()
    val selectedType by viewModel.selectedAdapterType.collectAsState()
    val classicAdapters by viewModel.classicAdapters.collectAsState()
    val bleAdapters by viewModel.bleAdapters.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val wifiHost by viewModel.wifiHost.collectAsState()
    val wifiPort by viewModel.wifiPort.collectAsState()
    val showAddVehicle by viewModel.showAddVehicle.collectAsState()
    val showAdapterGuide by viewModel.showAdapterGuide.collectAsState()
    val connectError by viewModel.connectError.collectAsState()

    LaunchedEffect(Unit) { viewModel.refreshClassicAdapters() }

    if (showAdapterGuide) {
        AdapterGuideScreen(onClose = { viewModel.hideAdapterGuide() })
        return
    }

    if (showAddVehicle) {
        AddVehicleDialog(
            onDismiss = { viewModel.hideAddVehicle() },
            onAdd = { n, mk, md, y -> viewModel.addVehicle(n, mk, md, y) }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().background(Charcoal).padding(16.dp)) {
        item {
            Text("JED", color = OrangeAccent, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Text("OBD-II Diagnostic Tool", color = LightGray, fontSize = 14.sp)
            Spacer(Modifier.height(24.dp))
        }

        if (connectionState == ConnectionState.CONNECTED) {
            item { ConnectedCard(deviceName, adapterType, onDisconnect = { viewModel.disconnect() }) }
        } else {
            item {
                ConnectSection(
                    selectedType = selectedType,
                    classicAdapters = classicAdapters,
                    bleAdapters = bleAdapters,
                    isScanning = isScanning,
                    isConnecting = connectionState == ConnectionState.CONNECTING ||
                        connectionState == ConnectionState.RECONNECTING,
                    wifiHost = wifiHost,
                    wifiPort = wifiPort,
                    connectError = connectError,
                    onSelectType = { viewModel.selectAdapterType(it) },
                    onRefreshClassic = { viewModel.refreshClassicAdapters() },
                    onScanBle = { viewModel.scanBleAdapters() },
                    onConnect = { viewModel.connect(it) },
                    onWifiHost = { viewModel.setWifiHost(it) },
                    onWifiPort = { viewModel.setWifiPort(it) },
                    onConnectWifi = { viewModel.connectWifi() }
                )
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { viewModel.showAdapterGuide() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = OrangeAccent)
                Spacer(Modifier.width(8.dp))
                Text("Which adapter should I buy?", color = OffWhite, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("VEHICLES", color = OffWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                FloatingActionButton(
                    onClick = { viewModel.showAddVehicle() },
                    containerColor = OrangeAccent, modifier = Modifier.size(40.dp)
                ) { Icon(Icons.Default.Add, contentDescription = "Add vehicle", tint = Charcoal) }
            }
            Spacer(Modifier.height(8.dp))
        }

        items(vehicles) { vehicle ->
            VehicleCard(vehicle, onClick = { onNavigateToDashboard() })
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ConnectedCard(deviceName: String, adapterType: AdapterType?, onDisconnect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(GreenGood.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .border(1.dp, GreenGood, RoundedCornerShape(8.dp)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = GreenGood)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Connected", color = GreenGood, fontWeight = FontWeight.Bold)
            Text(
                "$deviceName  ·  ${adapterType?.displayName ?: ""}",
                color = LightGray, fontSize = 11.sp
            )
        }
        Button(
            onClick = onDisconnect,
            colors = ButtonDefaults.buttonColors(containerColor = RedAlert)
        ) { Text("Disconnect", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun ConnectSection(
    selectedType: AdapterType,
    classicAdapters: List<com.jed.app.transport.AdapterTarget.Classic>,
    bleAdapters: List<com.jed.app.transport.AdapterTarget.Ble>,
    isScanning: Boolean,
    isConnecting: Boolean,
    wifiHost: String,
    wifiPort: String,
    connectError: String?,
    onSelectType: (AdapterType) -> Unit,
    onRefreshClassic: () -> Unit,
    onScanBle: () -> Unit,
    onConnect: (com.jed.app.transport.AdapterTarget) -> Unit,
    onWifiHost: (String) -> Unit,
    onWifiPort: (String) -> Unit,
    onConnectWifi: () -> Unit
) {
    Column {
        Text("CONNECT ADAPTER", color = OffWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AdapterTypeChip("Bluetooth", AdapterType.BLUETOOTH_CLASSIC, selectedType, onSelectType)
            AdapterTypeChip("BLE", AdapterType.BLUETOOTH_LE, selectedType, onSelectType)
            AdapterTypeChip("Wi-Fi", AdapterType.WIFI, selectedType, onSelectType)
        }
        Spacer(Modifier.height(8.dp))

        connectError?.let {
            Text(it, color = RedAlert, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        }
        if (isConnecting) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                CircularProgressIndicator(color = OrangeAccent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Connecting...", color = YellowWarn, fontSize = 13.sp)
            }
        }

        when (selectedType) {
            AdapterType.BLUETOOTH_CLASSIC -> {
                ScanRow("Paired Bluetooth adapters", Icons.Default.Refresh, "Refresh", onRefreshClassic)
                if (classicAdapters.isEmpty()) {
                    Text(
                        "No paired devices. Pair your adapter in Android Settings first.",
                        color = LightGray, fontSize = 13.sp
                    )
                }
                classicAdapters.forEach { target ->
                    AdapterButton(Icons.Default.Bluetooth, target.displayName, target.address) {
                        onConnect(target)
                    }
                }
            }
            AdapterType.BLUETOOTH_LE -> {
                ScanRow("Nearby BLE adapters", Icons.Default.Refresh, "Scan", onScanBle)
                if (isScanning) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = OrangeAccent, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Scanning...", color = LightGray, fontSize = 13.sp)
                    }
                }
                if (!isScanning && bleAdapters.isEmpty()) {
                    Text(
                        "Tap Scan to search for nearby BLE adapters.",
                        color = LightGray, fontSize = 13.sp
                    )
                }
                bleAdapters.forEach { target ->
                    AdapterButton(Icons.Default.Bluetooth, target.displayName, target.address) {
                        onConnect(target)
                    }
                }
            }
            AdapterType.WIFI -> {
                val colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeAccent, unfocusedBorderColor = MediumGray,
                    focusedTextColor = OffWhite, unfocusedTextColor = OffWhite,
                    cursorColor = OrangeAccent, focusedLabelColor = OrangeAccent,
                    unfocusedLabelColor = LightGray
                )
                Text(
                    "Join the adapter's Wi-Fi network first, then connect.",
                    color = LightGray, fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = wifiHost, onValueChange = onWifiHost,
                        label = { Text("Host / IP") }, colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = wifiPort, onValueChange = { onWifiPort(it.filter { c -> c.isDigit() }) },
                        label = { Text("Port") }, colors = colors,
                        modifier = Modifier.width(110.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onConnectWifi,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = null, tint = Charcoal)
                    Spacer(Modifier.width(8.dp))
                    Text("Connect", color = Charcoal, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AdapterTypeChip(
    label: String,
    type: AdapterType,
    selected: AdapterType,
    onSelect: (AdapterType) -> Unit
) {
    FilterChip(
        selected = selected == type,
        onClick = { onSelect(type) },
        label = { Text(label, fontWeight = FontWeight.Bold) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = OrangeAccent, containerColor = MediumGray
        )
    )
}

@Composable
private fun ScanRow(title: String, icon: ImageVector, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = LightGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.clickable { onAction() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = action, tint = OrangeAccent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(action, color = OrangeAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AdapterButton(icon: ImageVector, name: String, subtitle: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(56.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = OrangeAccent)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(name, color = OffWhite, fontWeight = FontWeight.Bold)
            Text(subtitle, color = LightGray, fontSize = 11.sp)
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
    var year by remember { mutableIntStateOf(2010) }
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
            OutlinedTextField(
                value = nickname, onValueChange = { nickname = it },
                label = { Text("Nickname") }, colors = textFieldColors, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(expanded = makeExpanded, onExpandedChange = { makeExpanded = it }) {
                OutlinedTextField(
                    value = make, onValueChange = {}, readOnly = true,
                    label = { Text("Make") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = makeExpanded) },
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = makeExpanded, onDismissRequest = { makeExpanded = false }) {
                    VehicleCatalog.MAKES.forEach { m ->
                        DropdownMenuItem(text = { Text(m) }, onClick = { make = m; makeExpanded = false })
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = model, onValueChange = { model = it },
                label = { Text("Model") }, colors = textFieldColors, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = year.toString(),
                onValueChange = { it.toIntOrNull()?.let { y -> year = y } },
                label = { Text("Year") }, colors = textFieldColors, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = MediumGray)) {
                    Text("Cancel")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { onAdd(nickname, make, model, year) },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    enabled = nickname.isNotBlank() && model.isNotBlank()
                ) { Text("Add", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

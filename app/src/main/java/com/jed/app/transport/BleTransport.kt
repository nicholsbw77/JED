package com.jed.app.transport

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bluetooth LE link for ELM327 BLE adapters (Vgate vLinker, many "OBDII BLE"
 * clones). These expose a serial-over-GATT service: one characteristic to
 * write commands, one that notifies responses. We auto-detect the right pair
 * across the common vendor UUIDs.
 */
@Singleton
class BleTransport @Inject constructor(
    @ApplicationContext private val context: Context
) : ObdTransport {

    companion object {
        private const val CONNECT_TIMEOUT_MS = 15_000L
        private const val CHUNK_SIZE = 20
        private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        // Notify/write characteristics used by common ELM327 BLE adapters,
        // in preference order (HM-10 clones, FFE0/FFF0 serial, Nordic UART).
        private val PREFERRED_NOTIFY = setOf("0000fff1", "0000ffe1", "6e400003")
        private val PREFERRED_WRITE = setOf("0000fff2", "0000ffe1", "6e400002")
    }

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private var gatt: BluetoothGatt? = null
    private var writeChar: BluetoothGattCharacteristic? = null
    private var writeNoResponse = false
    private var connectDeferred: CompletableDeferred<Boolean>? = null

    private val bufferLock = Any()
    private val buffer = StringBuilder()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState

    @SuppressLint("MissingPermission")
    suspend fun scan(durationMs: Long = 6000): List<AdapterTarget.Ble> =
        withContext(Dispatchers.IO) {
            val scanner = bluetoothAdapter?.bluetoothLeScanner ?: return@withContext emptyList()
            val found = LinkedHashMap<String, AdapterTarget.Ble>()
            val callback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    val device = result.device
                    val name = device.name ?: result.scanRecord?.deviceName ?: ""
                    found[device.address] = AdapterTarget.Ble(name = name, address = device.address)
                }
            }
            try {
                scanner.startScan(callback)
                delay(durationMs)
            } finally {
                scanner.stopScan(callback)
            }
            // Named devices first — adapters almost always advertise a name.
            found.values.sortedByDescending { it.name.isNotBlank() }
        }

    @SuppressLint("MissingPermission")
    override suspend fun connect(target: AdapterTarget): Boolean = withContext(Dispatchers.IO) {
        if (target !is AdapterTarget.Ble) return@withContext false
        val adapter = bluetoothAdapter ?: return@withContext false
        _connectionState.value = ConnectionState.CONNECTING
        synchronized(bufferLock) { buffer.setLength(0) }

        val device = try {
            adapter.getRemoteDevice(target.address)
        } catch (e: IllegalArgumentException) {
            _connectionState.value = ConnectionState.DISCONNECTED
            return@withContext false
        }

        val deferred = CompletableDeferred<Boolean>()
        connectDeferred = deferred
        gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)

        val ok = withTimeoutOrNull(CONNECT_TIMEOUT_MS) { deferred.await() } ?: false
        if (ok) {
            _connectionState.value = ConnectionState.CONNECTED
        } else {
            disconnect()
        }
        ok
    }

    @SuppressLint("MissingPermission")
    override fun disconnect() {
        try {
            gatt?.disconnect()
            gatt?.close()
        } catch (_: Exception) {
        }
        gatt = null
        writeChar = null
        connectDeferred = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    override suspend fun send(data: ByteArray) = withContext(Dispatchers.IO) {
        val activeGatt = gatt ?: throw IOException("Not connected")
        val ch = writeChar ?: throw IOException("No write characteristic")
        var offset = 0
        while (offset < data.size) {
            val end = minOf(offset + CHUNK_SIZE, data.size)
            ch.writeType = if (writeNoResponse) {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            } else {
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            }
            ch.value = data.copyOfRange(offset, end)
            activeGatt.writeCharacteristic(ch)
            offset = end
            delay(20)
        }
    }

    override suspend fun readUntilPrompt(timeoutMs: Long): String = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val result = synchronized(bufferLock) {
                val idx = buffer.indexOf(">")
                if (idx >= 0) {
                    val s = buffer.substring(0, idx)
                    buffer.delete(0, idx + 1)
                    s
                } else {
                    null
                }
            }
            if (result != null) return@withContext result.trim()
            delay(10)
        }
        synchronized(bufferLock) {
            val s = buffer.toString()
            buffer.setLength(0)
            s.trim()
        }
    }

    private val gattCallback = object : android.bluetooth.BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                g.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectDeferred?.complete(false)
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        }

        @SuppressLint("MissingPermission")
        @Suppress("DEPRECATION")
        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                connectDeferred?.complete(false)
                return
            }
            val (notify, write, noResponse) = selectCharacteristics(g)
            if (notify == null || write == null) {
                connectDeferred?.complete(false)
                return
            }
            writeChar = write
            writeNoResponse = noResponse

            g.setCharacteristicNotification(notify, true)
            val cccd = notify.getDescriptor(CCCD_UUID)
            if (cccd != null) {
                cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                g.writeDescriptor(cccd)
            } else {
                // No CCCD; notifications may still flow on some clones.
                connectDeferred?.complete(true)
            }
        }

        override fun onDescriptorWrite(
            g: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            connectDeferred?.complete(status == BluetoothGatt.GATT_SUCCESS)
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val value = characteristic.value ?: return
            val text = String(value, Charsets.US_ASCII)
            synchronized(bufferLock) { buffer.append(text) }
        }
    }

    private data class CharSelection(
        val notify: BluetoothGattCharacteristic?,
        val write: BluetoothGattCharacteristic?,
        val writeNoResponse: Boolean
    )

    private fun selectCharacteristics(g: BluetoothGatt): CharSelection {
        var notify: BluetoothGattCharacteristic? = null
        var write: BluetoothGattCharacteristic? = null
        var noResponse = false

        for (service in g.services) {
            for (ch in service.characteristics) {
                val uuidPrefix = ch.uuid.toString().substring(0, 8)
                val props = ch.properties
                val canNotify = props and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0 ||
                    props and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0
                val canWrite = props and BluetoothGattCharacteristic.PROPERTY_WRITE != 0 ||
                    props and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0

                if (canNotify && (notify == null || uuidPrefix in PREFERRED_NOTIFY)) {
                    notify = ch
                }
                if (canWrite && (write == null || uuidPrefix in PREFERRED_WRITE)) {
                    write = ch
                    noResponse = props and BluetoothGattCharacteristic.PROPERTY_WRITE == 0
                }
            }
        }
        return CharSelection(notify, write, noResponse)
    }
}

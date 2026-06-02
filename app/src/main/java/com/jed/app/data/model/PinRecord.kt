package com.jed.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pin_records")
data class PinRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val vin: String,
    val encryptedPin: String,
    val system: String,
    val retrievedAt: Long = System.currentTimeMillis()
)

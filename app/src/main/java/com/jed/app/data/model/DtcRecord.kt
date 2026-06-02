package com.jed.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dtc_history")
data class DtcRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val code: String,
    val description: String,
    val severity: String,
    val detectedAt: Long = System.currentTimeMillis(),
    val clearedAt: Long? = null
)

package com.jed.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_logs")
data class DataLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val sessionId: String,
    val timestamp: Long,
    val pidCode: String,
    val pidName: String,
    val value: Float,
    val unit: String
)

package com.jed.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nickname: String,
    val make: String,
    val model: String,
    val year: Int,
    val vin: String = "",
    val detectedProtocol: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

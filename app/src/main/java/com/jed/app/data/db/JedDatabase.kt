package com.jed.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jed.app.data.model.DataLogEntry
import com.jed.app.data.model.DtcRecord
import com.jed.app.data.model.PinRecord
import com.jed.app.data.model.Vehicle

@Database(
    entities = [Vehicle::class, DataLogEntry::class, DtcRecord::class, PinRecord::class],
    version = 1,
    exportSchema = false
)
abstract class JedDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun dataLogDao(): DataLogDao
    abstract fun dtcHistoryDao(): DtcHistoryDao
    abstract fun pinStorageDao(): PinStorageDao
}

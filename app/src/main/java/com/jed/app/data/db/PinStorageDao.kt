package com.jed.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jed.app.data.model.PinRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PinStorageDao {
    @Query("SELECT * FROM pin_records WHERE vehicleId = :vehicleId ORDER BY retrievedAt DESC")
    fun getForVehicle(vehicleId: Long): Flow<List<PinRecord>>

    @Query("SELECT * FROM pin_records ORDER BY retrievedAt DESC")
    fun getAll(): Flow<List<PinRecord>>

    @Insert
    suspend fun insert(record: PinRecord)
}

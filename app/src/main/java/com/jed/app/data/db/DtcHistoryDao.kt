package com.jed.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jed.app.data.model.DtcRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DtcHistoryDao {
    @Query("SELECT * FROM dtc_history WHERE vehicleId = :vehicleId ORDER BY detectedAt DESC")
    fun getForVehicle(vehicleId: Long): Flow<List<DtcRecord>>

    @Insert
    suspend fun insert(record: DtcRecord)

    @Insert
    suspend fun insertAll(records: List<DtcRecord>)

    @Query("UPDATE dtc_history SET clearedAt = :clearedAt WHERE vehicleId = :vehicleId AND clearedAt IS NULL")
    suspend fun markAllCleared(vehicleId: Long, clearedAt: Long = System.currentTimeMillis())
}

package com.jed.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jed.app.data.model.DataLogEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DataLogDao {
    @Insert
    suspend fun insert(entry: DataLogEntry)

    @Insert
    suspend fun insertAll(entries: List<DataLogEntry>)

    @Query("SELECT * FROM data_logs WHERE vehicleId = :vehicleId AND sessionId = :sessionId ORDER BY timestamp")
    fun getSession(vehicleId: Long, sessionId: String): Flow<List<DataLogEntry>>

    @Query("SELECT DISTINCT sessionId FROM data_logs WHERE vehicleId = :vehicleId ORDER BY timestamp DESC")
    suspend fun getSessionIds(vehicleId: Long): List<String>

    @Query("DELETE FROM data_logs WHERE vehicleId = :vehicleId AND sessionId = :sessionId")
    suspend fun deleteSession(vehicleId: Long, sessionId: String)
}

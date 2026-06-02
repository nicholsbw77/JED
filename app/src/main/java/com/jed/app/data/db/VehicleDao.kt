package com.jed.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.jed.app.data.model.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getById(id: Long): Vehicle?

    @Query("SELECT * FROM vehicles WHERE vin = :vin LIMIT 1")
    suspend fun getByVin(vin: String): Vehicle?

    @Insert
    suspend fun insert(vehicle: Vehicle): Long

    @Update
    suspend fun update(vehicle: Vehicle)

    @Delete
    suspend fun delete(vehicle: Vehicle)
}

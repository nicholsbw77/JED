package com.jed.app.data

import android.content.Context
import androidx.room.Room
import com.jed.app.data.db.DataLogDao
import com.jed.app.data.db.DtcHistoryDao
import com.jed.app.data.db.JedDatabase
import com.jed.app.data.db.PinStorageDao
import com.jed.app.data.db.VehicleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): JedDatabase {
        return Room.databaseBuilder(context, JedDatabase::class.java, "jed.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideVehicleDao(db: JedDatabase): VehicleDao = db.vehicleDao()
    @Provides fun provideDataLogDao(db: JedDatabase): DataLogDao = db.dataLogDao()
    @Provides fun provideDtcHistoryDao(db: JedDatabase): DtcHistoryDao = db.dtcHistoryDao()
    @Provides fun providePinStorageDao(db: JedDatabase): PinStorageDao = db.pinStorageDao()
}

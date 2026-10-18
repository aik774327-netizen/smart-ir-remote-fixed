package com.kyawzay.blueconnect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ApplianceIrCodeEntity::class], version = 1, exportSchema = false)
abstract class ApplianceDatabase : RoomDatabase() {

    abstract fun applianceIrDao(): ApplianceIrDao

    companion object {
        @Volatile
        private var instance: ApplianceDatabase? = null

        fun getInstance(context: Context): ApplianceDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ApplianceDatabase::class.java,
                    "appliance_ir.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}

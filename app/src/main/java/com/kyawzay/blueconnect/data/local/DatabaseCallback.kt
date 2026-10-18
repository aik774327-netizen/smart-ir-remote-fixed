package com.kyawzay.blueconnect.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class DatabaseCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val presets = listOf(
            arrayOf("Keruilai", "Fan", "Universal_AirCooler", "POWER", "0x00FF00FF"),
            arrayOf("KENNEDE", "Fan", "KN-5996HRB", "POWER", "0x00FE01FE"),
            arrayOf("Midea", "Fan", "FS40 Stand Fan", "POWER", "0x01FE02FD"),
            arrayOf("Midea", "Fan", "FS40 Stand Fan", "SPEED", "0x01FE827D"),
            arrayOf("Gree", "Air Conditioner", "Fairy Inverter", "POWER", "0x00FF18E7"),
            arrayOf("Gree", "Air Conditioner", "Fairy Inverter", "TEMP_UP", "0x00FF58A7"),
            arrayOf("Xiaomi", "Fan", "Smart Standing Fan 2", "POWER", "0x00FF00FF"),
            arrayOf("Xiaomi", "Fan", "Smart Standing Fan 2", "SPEED", "0x00FF807F"),
            arrayOf("Xiaomi", "TV", "Mi TV 4A/4S", "POWER", "0x00FF12ED")
        )

        db.beginTransaction()
        try {
            presets.forEach { (brand, category, model, button, hex) ->
                db.execSQL(
                    "INSERT INTO appliance_ir_codes " +
                        "(brandName, category, modelName, buttonName, prontoHex, rawPatternCsv, hexCode, frequency) " +
                        "VALUES (?, ?, ?, ?, NULL, NULL, ?, ?)",
                    arrayOf(brand, category, model, button, hex, 38_000)
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}

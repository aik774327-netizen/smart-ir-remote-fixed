package com.kyawzay.blueconnect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appliance_ir_codes")
data class ApplianceIrCodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val brandName: String,
    val category: String,
    val modelName: String,
    val buttonName: String,
    val prontoHex: String? = null,
    val rawPatternCsv: String? = null,
    val hexCode: String? = null,
    val frequency: Int = 38000
)

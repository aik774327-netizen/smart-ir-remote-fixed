package com.kyawzay.blueconnect.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplianceIrDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIrCodes(codes: List<ApplianceIrCodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIrCode(code: ApplianceIrCodeEntity): Long

    @Query("SELECT * FROM appliance_ir_codes WHERE brandName = :brand AND category = :category")
    fun getCodesByBrandAndCategory(brand: String, category: String): Flow<List<ApplianceIrCodeEntity>>

    @Query("SELECT * FROM appliance_ir_codes WHERE category = :category")
    fun getCodesByCategory(category: String): Flow<List<ApplianceIrCodeEntity>>

    @Query("SELECT DISTINCT brandName FROM appliance_ir_codes WHERE category = :category")
    fun getBrandsByCategory(category: String): Flow<List<String>>

    @Query("SELECT * FROM appliance_ir_codes")
    fun getAllCodes(): Flow<List<ApplianceIrCodeEntity>>

    @Query("DELETE FROM appliance_ir_codes WHERE id = :id")
    suspend fun deleteById(id: Long)
}

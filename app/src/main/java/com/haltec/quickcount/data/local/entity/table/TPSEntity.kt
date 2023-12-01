package com.haltec.quickcount.data.local.entity.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.haltec.quickcount.data.remote.response.TPSResponse
import com.haltec.quickcount.util.capitalizeWords
import com.haltec.quickcount.domain.model.TPS

const val TPS_TABLE = "TPS"
@Entity(tableName = TPS_TABLE)
data class TPSEntity(
    @ColumnInfo("village_code")
    val villageCode: String,
    val address: String,
    val city: String,
    val latitude: String,
    val pending: String,
    val dpt: Int,
    @ColumnInfo("created_at")
    val createdAt: String,
    @ColumnInfo("created_by")
    val createdBy: String,
    val approved: String,
    val subdistrict: String,
    val province: String,
    val name: String,
    @PrimaryKey
    val id: Int,
    val submitted: String,
    val rejected: String,
    val village: String,
    val longitude: String,
    @ColumnInfo("wait_to_be_sent")
    val waitToBeSent: String = "0"
){
    fun toModel() = TPS(
        villageCode,
        address,
        city,
        latitude,
        pending.toIntOrNull() ?: 0,
        dpt,
        createdAt,
        createdBy,
        approved.toIntOrNull() ?: 0,
        capitalizeWords(subdistrict),
        capitalizeWords(province),
        capitalizeWords(name),
        id,
        submitted.toIntOrNull() ?: 0,
        capitalizeWords(village),
        longitude,
        rejected.toIntOrNull() ?: 0,
        waitToBeSent.toIntOrNull() ?: 0
    )
    
    fun toEntity() = TPSEntity(
        villageCode, address, city, latitude, pending, dpt, createdAt, createdBy, approved, subdistrict, province, name, id, submitted, rejected, village, longitude
    )
}

fun List<TPSResponse>.toEntity(): List<TPSEntity>{
    return this.map { it.toEntity() }.filter { it.id != 0 }
}

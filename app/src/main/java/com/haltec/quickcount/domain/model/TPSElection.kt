package com.haltec.quickcount.domain.model

import com.haltec.quickcount.data.util.lowerAllWords

fun stringToFilter(value: String): String{
    return when(lowerAllWords(value)){
        "belum dikirim" -> "pending"
        "belum terverifikasi" -> "submitted"
        "sudah terverifikasi" -> "approved"
        else -> ""
    }
}

data class TPSElection(
    val villageCode: String,
    val address: String,
    val city: String,
    val latitude: String,
    val dpt: Int,
    val createdAt: String,
    val createdBy: String,
    val electionName: String,
    val subdistrict: String,
    val province: String,
    val electionId: Int,
    val tpsName: String,
    val tpsId: Int,
    val village: String,
    val longitude: String,
    val statusVote: String
){
    val statusVoteNote: String
        get() = when(statusVote){
            "0" -> BELUM_TERVERIFIKASI
            "1" -> SUDAH_TERVERIFIKASI
            else -> BELUM_DIKIRIM
        }
    
    fun toTPS() = TPS(
        id = tpsId,
        name = tpsName,
        address = address,
        villageCode = villageCode,
        village = village,
        subdistrict = subdistrict,
        province = province,
        city = city,
        latitude = latitude,
        longitude = longitude,
        approved = if (statusVote == "1") "1" else "0",
        submitted = if (statusVote == "0") "1" else "0",
        pending = if (statusVote == "") "1" else "0",
        dpt = dpt,
        createdBy = createdBy,
        createdAt = createdAt
    )
    
    fun toElection() = Election(
        updatedAt = "",
        statusVote = statusVote,
        updatedBy = "",
        active = 1,
        createdAt = createdAt,
        id = electionId,
        title = electionName,
        createdBy = createdBy
    )
}

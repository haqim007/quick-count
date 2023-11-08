package com.haltec.quickcount.domain.model

enum class ElectionFilter{
    BELUM_DIKIRIM,
    BELUM_TERVERIFIKASI,
    SUDAH_TERVERIFIKASI,
    DITOLAK,
    SEMUA
}

val ElectionFilter.text
    get() = when(this){
        ElectionFilter.BELUM_DIKIRIM -> ElectionStatus.PENDING.text
        ElectionFilter.BELUM_TERVERIFIKASI -> ElectionStatus.SUBMITTED.text
        ElectionFilter.SUDAH_TERVERIFIKASI -> ElectionStatus.VERIFIED.text
        ElectionFilter.DITOLAK -> ElectionStatus.REJECTED.text
        else -> "Semua"
    }

fun stringToFilter(value: String): String {
    return when (value) {
        ElectionStatus.PENDING.text -> ElectionStatus.PENDING.valueText
        ElectionStatus.SUBMITTED.text -> ElectionStatus.SUBMITTED.valueText
        ElectionStatus.VERIFIED.text -> ElectionStatus.VERIFIED.valueText
        ElectionStatus.REJECTED.text -> ElectionStatus.REJECTED.valueText
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
    val statusVote: ElectionStatus
){
    
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
        approved = null,
        submitted = null,
        pending = null,
        dpt = dpt,
        createdBy = createdBy,
        createdAt = createdAt,
        rejected = null
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

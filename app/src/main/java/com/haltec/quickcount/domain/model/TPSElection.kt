package com.haltec.quickcount.domain.model

enum class ElectionFilter{
    BELUM_DIKIRIM,
    BELUM_TERVERIFIKASI,
    SUDAH_TERVERIFIKASI,
    MENUNGGU_TERKIRIM,
    DITOLAK,
    SEMUA
}

val ElectionFilter.text
    get() = when(this){
        ElectionFilter.BELUM_DIKIRIM -> SubmitVoteStatus.PENDING.text
        ElectionFilter.BELUM_TERVERIFIKASI -> SubmitVoteStatus.SUBMITTED.text
        ElectionFilter.MENUNGGU_TERKIRIM -> SubmitVoteStatus.IN_QUEUE.text
        ElectionFilter.SUDAH_TERVERIFIKASI -> SubmitVoteStatus.VERIFIED.text
        ElectionFilter.DITOLAK -> SubmitVoteStatus.REJECTED.text
        else -> "Semua"
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
    val statusVote: SubmitVoteStatus
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

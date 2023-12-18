package com.haltec.quickcount.domain.model

enum class ElectionFilter{
    BELUM_DIKIRIM,
    BELUM_TERVERIFIKASI,
    SUDAH_TERVERIFIKASI,
    MENUNGGU_TERKIRIM,
    DITOLAK,
    SEMUA
}

fun stringToElectionFilter(text: String): ElectionFilter{
    return when(text){
        SubmitVoteStatus.PENDING.label -> ElectionFilter.BELUM_DIKIRIM
        SubmitVoteStatus.SUBMITTED.label -> ElectionFilter.BELUM_TERVERIFIKASI
        SubmitVoteStatus.IN_QUEUE.label -> ElectionFilter.MENUNGGU_TERKIRIM
        SubmitVoteStatus.VERIFIED.label -> ElectionFilter.SUDAH_TERVERIFIKASI
        SubmitVoteStatus.REJECTED.label -> ElectionFilter.DITOLAK
        else -> ElectionFilter.SEMUA
    }
}

val ElectionFilter.text
    get() = when(this){
        ElectionFilter.BELUM_DIKIRIM -> SubmitVoteStatus.PENDING.label
        ElectionFilter.BELUM_TERVERIFIKASI -> SubmitVoteStatus.SUBMITTED.label
        ElectionFilter.MENUNGGU_TERKIRIM -> SubmitVoteStatus.IN_QUEUE.label
        ElectionFilter.SUDAH_TERVERIFIKASI -> SubmitVoteStatus.VERIFIED.label
        ElectionFilter.DITOLAK -> SubmitVoteStatus.REJECTED.label
        else -> "Semua"
    }

val ElectionFilter.submitVoteStatus: SubmitVoteStatus?
    get() = when(this){
        ElectionFilter.BELUM_DIKIRIM -> SubmitVoteStatus.PENDING
        ElectionFilter.BELUM_TERVERIFIKASI -> SubmitVoteStatus.SUBMITTED
        ElectionFilter.MENUNGGU_TERKIRIM -> SubmitVoteStatus.IN_QUEUE
        ElectionFilter.SUDAH_TERVERIFIKASI -> SubmitVoteStatus.VERIFIED
        ElectionFilter.DITOLAK -> SubmitVoteStatus.REJECTED
        else -> null
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
    val statusVote: SubmitVoteStatus,
    val lastVoteUpdated: String
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
        active = 1,
        createdAt = createdAt,
        id = electionId,
        title = electionName,
        createdBy = createdBy
    )
}

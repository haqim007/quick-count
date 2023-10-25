package com.haltec.quickcount.domain.model

data class Election(
    val updatedAt: String,
    val statusVote: String,
    val updatedBy: String,
    val active: Int,
    val createdAt: String,
    val id: Int,
    val title: String,
    val createdBy: String
){
    val statusVoteNote: String = when(statusVote){
        "0" -> "Belum Terverifikasi"
        "1" -> "Sudah Terverifikasi"
        else -> "Belum Dikirim"
    }
}
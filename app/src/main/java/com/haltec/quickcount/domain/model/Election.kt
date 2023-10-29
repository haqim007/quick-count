package com.haltec.quickcount.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Election(
    val updatedAt: String,
    val statusVote: String,
    val updatedBy: String,
    val active: Int,
    val createdAt: String,
    val id: Int,
    val title: String,
    val createdBy: String
): Parcelable

const val BELUM_TERVERIFIKASI = "Belum Terverifikasi"
const val SUDAH_TERVERIFIKASI = "Sudah Terverifikasi"
const val BELUM_DIKIRIM = "Belum Dikirim"

val Election.statusVoteNote: String
    get() = when(statusVote){
        "0" -> BELUM_TERVERIFIKASI
        "1" -> SUDAH_TERVERIFIKASI
        else -> BELUM_DIKIRIM
    }
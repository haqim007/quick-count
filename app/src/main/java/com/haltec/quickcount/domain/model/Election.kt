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

val Election.statusVoteNote: String
    get() = when(statusVote){
        ElectionStatus.SUBMITTED.valueNumber -> ElectionStatus.SUBMITTED.text
        ElectionStatus.VERIFIED.valueNumber -> ElectionStatus.VERIFIED.text
        ElectionStatus.REJECTED.valueNumber -> ElectionStatus.REJECTED.text
        else -> ElectionStatus.PENDING.text
    }
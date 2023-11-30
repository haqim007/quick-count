package com.haltec.quickcount.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Election(
    val updatedAt: String,
    val statusVote: SubmitVoteStatus,
    val updatedBy: String,
    val active: Int,
    val createdAt: String,
    val id: Int,
    val title: String,
    val createdBy: String
): Parcelable
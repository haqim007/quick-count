package com.haltec.quickcount.data.local.entity.table

import com.google.gson.annotations.SerializedName

data class TempVoteCandidateEntity(
    @SerializedName("candidate_id")
    val candidateId: Int,
    val amount: Int
)

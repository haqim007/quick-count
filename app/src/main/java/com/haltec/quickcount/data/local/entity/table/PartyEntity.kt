package com.haltec.quickcount.data.local.entity.table

import com.google.gson.annotations.SerializedName
data class PartyEntity(
    val partaiName: String,
    val id: Int,
    val amount: Int,
    @SerializedName("candidate_list")
    val candidateList: List<CandidateEntity>,
    val noUrut: Int
)

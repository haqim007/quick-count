package com.haltec.quickcount.data.local.entity.table

import com.google.gson.annotations.SerializedName
data class PartyEntity(
    @SerializedName("partai_name")
    val partaiName: String,
    val id: Int,
    val amount: Int,
    @SerializedName("candidate_list")
    val candidateList: List<CandidateEntity>
)

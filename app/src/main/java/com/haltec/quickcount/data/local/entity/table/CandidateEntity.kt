package com.haltec.quickcount.data.local.entity.table

import com.google.gson.annotations.SerializedName
data class CandidateEntity(
    @SerializedName("no_urut")
    val noUrut: Int,
    val name: String,
    val id: Int,
    val amount: Int
)

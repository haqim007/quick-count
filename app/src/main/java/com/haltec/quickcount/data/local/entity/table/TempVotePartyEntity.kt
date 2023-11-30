package com.haltec.quickcount.data.local.entity.table

import com.google.gson.annotations.SerializedName

data class TempVotePartyEntity(
    @SerializedName("partai_id")
    val partaiId: Int,
    val amount: Int
)

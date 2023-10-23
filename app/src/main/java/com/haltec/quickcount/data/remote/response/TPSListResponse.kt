package com.haltec.quickcount.data.remote.response

import com.google.gson.annotations.SerializedName

data class TPSListResponse(

	@field:SerializedName("data")
	val data: List<TPSResponse>? = null,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("version")
	val version: String
)


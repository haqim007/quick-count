package com.haltec.quickcount.data.remote.response

import com.google.gson.annotations.SerializedName

data class BasicResponse(
	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("version")
	val version: String
)


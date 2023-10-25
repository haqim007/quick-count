package com.haltec.quickcount.data.remote.response

import com.google.gson.annotations.SerializedName

data class BasicResponse(

	@field:SerializedName("data")
	val data: TokenData? = null,
	
	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("version")
	val version: String = ""
){
	data class TokenData(
		@field:SerializedName("exp")
		val exp: String,

		@field:SerializedName("token")
		val token: String,
	)
}
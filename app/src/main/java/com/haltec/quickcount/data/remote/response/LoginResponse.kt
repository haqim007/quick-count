package com.haltec.quickcount.data.remote.response

import com.google.gson.annotations.SerializedName
import com.haltec.quickcount.util.DATE_TIME_FORMAT
import com.haltec.quickcount.util.capitalizeWords
import com.haltec.quickcount.util.stringToTimestamp
import com.haltec.quickcount.domain.model.Login
import com.haltec.quickcount.domain.model.UserInfo

data class LoginResponse(

	@field:SerializedName("data")
	val data: Data? = null,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("version")
	val version: String
){
	data class Data(

		@field:SerializedName("secret_key")
		val secretKey: String,

		@field:SerializedName("name")
		val name: String,

		@field:SerializedName("exp")
		val exp: String,

		@field:SerializedName("token")
		val token: String
	)
	
	fun toModel() = Login(
		userName = this.data?.name ?: ""
	)
	
	fun toUserInfo() = UserInfo(
		name = capitalizeWords(this.data?.name ?: ""),
		token = this.data?.token,
		expiredTimestamp = this.data?.exp?.let { stringToTimestamp(it, DATE_TIME_FORMAT) }
	)
}



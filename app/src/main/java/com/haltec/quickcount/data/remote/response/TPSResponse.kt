package com.haltec.quickcount.data.remote.response

import com.google.gson.annotations.SerializedName
import com.haltec.quickcount.data.util.capitalizeWords
import com.haltec.quickcount.domain.model.TPS

data class TPSResponse(

	@field:SerializedName("village_code")
	val villageCode: String,

	@field:SerializedName("address")
	val address: String,

	@field:SerializedName("city")
	val city: String,

	@field:SerializedName("latitude")
	val latitude: String,

	@field:SerializedName("pending")
	val pending: String,

	@field:SerializedName("dpt")
	val dpt: Int,

	@field:SerializedName("created_at")
	val createdAt: String,

	@field:SerializedName("created_by")
	val createdBy: String,

	@field:SerializedName("approved")
	val approved: String,

	@field:SerializedName("subdistrict")
	val subdistrict: String,

	@field:SerializedName("provice")
	val province: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("submited")
	val submited: String,

	@field:SerializedName("rejected")
	val rejected: String,

	@field:SerializedName("village")
	val village: String,

	@field:SerializedName("longitude")
	val longitude: String
){
	fun toModel() = TPS(
		villageCode, 
		address, 
		city, 
		latitude, 
		pending.toIntOrNull() ?: 0, 
		dpt, 
		createdAt, 
		createdBy, 
		approved.toIntOrNull() ?: 0,
		capitalizeWords(subdistrict),
		capitalizeWords(province), 
		capitalizeWords(name), 
		id, 
		submited.toIntOrNull() ?: 0,
		capitalizeWords(village), 
		longitude,
		rejected.toIntOrNull() ?: 0
	)
}
package com.haltec.quickcount.data.remote.response

import com.google.gson.annotations.SerializedName
import com.haltec.quickcount.data.util.capitalizeWords
import com.haltec.quickcount.data.util.stringToStringDateID
import com.haltec.quickcount.domain.model.TPSElection

data class TPSElectionListResponse(
	@field:SerializedName("data")
	val data: List<DataItem>? = null,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("version")
	val version: String
){
	data class DataItem(

		@field:SerializedName("village_code")
		val villageCode: String,

		@field:SerializedName("address")
		val address: String,

		@field:SerializedName("city")
		val city: String,

		@field:SerializedName("latitude")
		val latitude: String,

		@field:SerializedName("dpt")
		val dpt: Int,

		@field:SerializedName("created_at")
		val createdAt: String,

		@field:SerializedName("created_by")
		val createdBy: String,

		@field:SerializedName("selection_type")
		val selectionType: String,

		@field:SerializedName("subdistrict")
		val subdistrict: String,

		@field:SerializedName("provice")
		val provice: String,

		@field:SerializedName("selection_type_id")
		val selectionTypeId: String,

		@field:SerializedName("name")
		val name: String,

		@field:SerializedName("id")
		val id: Int,

		@field:SerializedName("village")
		val village: String,

		@field:SerializedName("longitude")
		val longitude: String,

		@field:SerializedName("status")
		val status: String
	){
		fun toModel() =
			TPSElection(
				tpsId = id,
				tpsName = name,
				electionId = selectionTypeId.toIntOrNull() ?: 0,
				electionName = selectionType,
				village = village,
				villageCode = villageCode,
				subdistrict = subdistrict,
				city = city,
				province = provice,
				longitude = longitude,
				latitude = latitude,
				createdAt = stringToStringDateID(createdAt),
				statusVote = status,
				createdBy = capitalizeWords(createdBy),
				address = address,
				dpt = dpt
			)
		
	}
	
	fun toModel() = this.data?.map { it.toModel() }
	
	
}

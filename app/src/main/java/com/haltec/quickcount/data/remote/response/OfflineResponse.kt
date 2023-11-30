package com.haltec.quickcount.data.remote.response

import com.google.gson.annotations.SerializedName
import com.haltec.quickcount.data.local.entity.table.TPSEntity

data class OfflineResponse(

	@field:SerializedName("data")
	val data: List<OfflineDataResponse>,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("version")
	val version: String
){
	data class OfflineDataResponse(

		@field:SerializedName("village_code")
		val villageCode: String,

		@field:SerializedName("address")
		val address: String,

		@field:SerializedName("city")
		val city: String,

		@field:SerializedName("rejected")
		val rejected: String,

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

		@field:SerializedName("selection_type_list")
		val selectionTypeList: List<ElectionListResponse.ElectionResponse>,

		@field:SerializedName("province")
		val province: String,

		@field:SerializedName("subdistrict")
		val subdistrict: String,

		@field:SerializedName("name")
		val name: String,

		@field:SerializedName("id")
		val id: Int,

		@field:SerializedName("submited")
		val submitted: String,

		@field:SerializedName("village")
		val village: String,

		@field:SerializedName("longitude")
		val longitude: String
	){
		fun toEntity() = TPSEntity(
			villageCode,
			address,
			city,
			latitude,
			pending,
			dpt,
			createdAt,
			createdBy,
			approved,
			subdistrict,
			province,
			name,
			id,
			submitted,
			rejected,
			village,
			longitude
		)
	}
}

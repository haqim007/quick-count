package com.haltec.quickcount.data.remote.response

import com.google.gson.annotations.SerializedName
import com.haltec.quickcount.data.util.stringToStringDateID
import com.haltec.quickcount.domain.model.VoteEvidence

data class CurrentEvidenceResponse(

	@field:SerializedName("data")
	val data: List<CurrentEvidenceData>? = null,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("version")
	val version: String
){
	data class CurrentEvidenceData(

		@field:SerializedName("tps_id")
		val tpsId: Int,

		@field:SerializedName("file")
		val file: String,

		@field:SerializedName("uploaded_at")
		val uploadedAt: String,

		@field:SerializedName("latitude")
		val latitude: String,

		@field:SerializedName("selection_type_id")
		val selectionTypeId: Int,

		@field:SerializedName("description")
		val description: String,

		@field:SerializedName("id")
		val id: Int,

		@field:SerializedName("type")
		val type: String,

		@field:SerializedName("longitude")
		val longitude: String
	){
		fun toModel() = VoteEvidence(
			this.latitude,
			this.description,
			"",
			this.type,
			"",
			this.tpsId,
			this.file,
			"",
			stringToStringDateID(this.uploadedAt),
			this.selectionTypeId,
			"",
			this.id,
			this.longitude
		)
	}
	
	fun toModel() = this.data?.map { it.toModel() } ?: emptyList()
}

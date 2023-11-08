package com.haltec.quickcount.data.remote.response

import com.google.gson.annotations.SerializedName
import com.haltec.quickcount.data.util.stringToStringDateID
import com.haltec.quickcount.domain.model.VoteEvidence

data class UploadEvidenceResponse(

	@field:SerializedName("data")
	val data: UploadEvidenceResultResponse,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("version")
	val version: String
){
	data class UploadEvidenceResultResponse(

		@field:SerializedName("latitude")
		val latitude: String,

		@field:SerializedName("description")
		val description: String,

		@field:SerializedName("created_at")
		val createdAt: String,

		@field:SerializedName("type")
		val type: String,

		@field:SerializedName("created_by")
		val createdBy: String,

		@field:SerializedName("tps_id")
		val tpsId: Int,

		@field:SerializedName("file")
		val file: String,

		@field:SerializedName("updated_at")
		val updatedAt: String,

		@field:SerializedName("uploaded_at")
		val uploadedAt: String,

		@field:SerializedName("selection_type_id")
		val selectionTypeId: Int,

		@field:SerializedName("updated_by")
		val updatedBy: String,

		@field:SerializedName("id")
		val id: Int,

		@field:SerializedName("longitude")
		val longitude: String
	)

	fun toModel() = VoteEvidence(
		this.data.latitude,
		this.data.description,
		stringToStringDateID(this.data.createdAt), 
		this.data.type,
		this.data.createdBy,
		this.data.tpsId,
		this.data.file,
		stringToStringDateID(this.data.updatedAt),
		stringToStringDateID(this.data.uploadedAt),
		this.data.selectionTypeId,
		this.data.updatedBy,
		this.data.id,
		this.data.longitude
	)
	
	
}

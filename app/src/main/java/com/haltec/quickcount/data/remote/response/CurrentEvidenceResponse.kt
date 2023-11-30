package com.haltec.quickcount.data.remote.response

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.haltec.quickcount.data.local.entity.table.UploadedEvidenceEntity
import com.haltec.quickcount.util.stringToStringDateID
import com.haltec.quickcount.domain.model.VoteEvidence
import com.haltec.quickcount.domain.model.valueToEvidenceTypeText
import com.haltec.quickcount.util.downloadImageToCacheDir
import com.haltec.quickcount.util.lowerAllWords

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
			valueToEvidenceTypeText(type),
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
		
		suspend fun toUploadedEvidenceEntity(context: Context) = UploadedEvidenceEntity(
			id = id,
			tpsId = tpsId,
			electionId = selectionTypeId,
			description = description,
			type = valueToEvidenceTypeText(type),
			fileUrl = file,
			file = downloadImageToCacheDir(context, file, "${lowerAllWords(type)}_${tpsId}_${selectionTypeId}"),
			latitude = latitude,
			longitude = longitude,
			uploadedAt = uploadedAt
		)

		fun toUploadedEvidenceEntity() = UploadedEvidenceEntity(
			id = id,
			tpsId = tpsId,
			electionId = selectionTypeId,
			description = description,
			type = valueToEvidenceTypeText(type),
			fileUrl = file,
			file = null,
			latitude = latitude,
			longitude = longitude,
			uploadedAt = uploadedAt
		)
		
	}
	
	fun toModel() = this.data?.map { it.toModel() } ?: emptyList()
	fun toUploadedEntities() = this.data?.map { it.toUploadedEvidenceEntity() } ?: emptyList()
	suspend fun toUploadedEntities(context: Context) = this.data?.map { it.toUploadedEvidenceEntity(context) } ?: emptyList()
}

package com.haltec.quickcount.data.remote.response

import com.google.gson.annotations.SerializedName
import com.haltec.quickcount.data.util.capitalizeWords
import com.haltec.quickcount.data.util.stringToDate
import com.haltec.quickcount.data.util.stringToStringDateID
import com.haltec.quickcount.domain.model.Election

data class ElectionListResponse(

	@field:SerializedName("data")
	val data: List<ElectionResponse>? = null,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("version")
	val version: String
){

	data class ElectionResponse(

		@field:SerializedName("updated_at")
		val updatedAt: String,

		@field:SerializedName("status_vote")
		val statusVote: String,

		@field:SerializedName("updated_by")
		val updatedBy: String,

		@field:SerializedName("active")
		val active: Int,

		@field:SerializedName("created_at")
		val createdAt: String,

		@field:SerializedName("id")
		val id: Int,

		@field:SerializedName("title")
		val title: String,

		@field:SerializedName("created_by")
		val createdBy: String
	)
	
	fun toModel() = this.data?.map { 
		Election(
			id = it.id,
			title = capitalizeWords(it.title),
			createdBy =  it.createdBy,
			createdAt = stringToStringDateID(it.createdAt),
			updatedBy =  it.updatedBy,
			updatedAt = stringToStringDateID(it.updatedAt),
			statusVote = it.statusVote,
			active = it.active
		)
	} ?: listOf()
}

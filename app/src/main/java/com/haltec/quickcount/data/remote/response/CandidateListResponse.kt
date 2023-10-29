package com.haltec.quickcount.data.remote.response

import com.google.gson.annotations.SerializedName
import com.haltec.quickcount.domain.model.VoteData

data class CandidateListResponse(

	@field:SerializedName("data")
	val data: CandidateListDataResponse,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("version")
	val version: String
){
	data class CandidateListDataResponse(

		@field:SerializedName("tps_id")
		val tpsId: Int,

		@field:SerializedName("province")
		val province: String,

		@field:SerializedName("subdistrict")
		val subdistrict: String,

		@field:SerializedName("partai_lists")
		val partaiLists: List<PartyListsItemResponse>,

		@field:SerializedName("village")
		val village: String,

		@field:SerializedName("tps_name")
		val tpsName: String
	)
	
	fun toModel(city: String): VoteData{
		val data = this.data
		return VoteData(
			tpsId = data.tpsId,
			tpsName = data.tpsName,
			city = city,
			partyLists = data.partaiLists.map { it.toModel() },
			province = data.province,
			subdistrict = data.subdistrict,
			village = data.village,
		)
	}
}

data class PartyListsItemResponse(

	@field:SerializedName("candidate_list")
	val candidateList: List<CandidateListItemResponse>,

	@field:SerializedName("partai_name")
	val partaiName: String,

	@field:SerializedName("id")
	val id: Int
){
	fun toModel() = VoteData.PartyListsItem(
		candidateList = this.candidateList.map { it.toModel() },
		partyName = partaiName,
		id
	)
}

data class CandidateListItemResponse(

	@field:SerializedName("no_urut")
	val noUrut: Int,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("id")
	val id: Int
){
	fun toModel() = VoteData.Candidate(
		orderNumber = noUrut,
		name,
		id
	)
}

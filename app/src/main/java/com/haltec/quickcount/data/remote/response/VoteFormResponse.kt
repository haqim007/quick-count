package com.haltec.quickcount.data.remote.response

import com.google.gson.annotations.SerializedName
import com.haltec.quickcount.data.local.entity.table.CandidateEntity
import com.haltec.quickcount.data.local.entity.table.PartyEntity
import com.haltec.quickcount.data.local.entity.table.VoteFormEntity
import com.haltec.quickcount.domain.model.VoteData

data class VoteFormResponse(

	@field:SerializedName("data")
	val data: VoteFormData,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("version")
	val version: String
){
	data class VoteFormData(

		@field:SerializedName("tps_id")
		val tpsId: Int,

		@field:SerializedName("selection_type_id")
		val selectionTypeId: Int,

		@field:SerializedName("province")
		val province: String,

		@field:SerializedName("subdistrict")
		val subdistrict: String,

		@field:SerializedName("is_partai")
		val isPartai: Int,
		
		@field:SerializedName("partai_lists")
		val partaiLists: List<PartyListsItemResponse>?,

		@field:SerializedName("village")
		val village: String,

		@field:SerializedName("tps_name")
		val tpsName: String,
		
		@field:SerializedName("amount")
		val amount: Int,

		@field:SerializedName("invalid_vote")
		val invalidVote: Int,

		@field:SerializedName("note")
		val note: String?,
	)
	
	fun toModel(city: String): VoteData{
		val data = this.data
		return VoteData(
			tpsId = data.tpsId,
			tpsName = data.tpsName,
			city = city,
			partyLists = data.partaiLists?.map { it.toModel() } ?: emptyList(),
			province = data.province,
			subdistrict = data.subdistrict,
			village = data.village,
			validVote = data.amount,
			invalidVote = data.invalidVote,
			note = data.note,
			isParty = data.isPartai == 1
		)
	}
	
	fun toEntity(): VoteFormEntity{
		this.data.apply {
			return VoteFormEntity(
				tpsId = tpsId,
				tpsName = tpsName,
				electionId = selectionTypeId,
				province = province,
				subdistrict = subdistrict,
				isPartai = isPartai,
				village = village,
				amount = amount,
				invalidVote = invalidVote,
				note = note,
				
				partaiList = partaiLists?.map { 
					PartyEntity(
						partaiName = it.partaiName,
						id = it.id,
						amount = it.amount,
						candidateList = it.candidateList.map { candidate ->
							CandidateEntity(
								noUrut = candidate.noUrut, 
								name = candidate.name, 
								id = candidate.id, amount)
						}
					)
				} ?: emptyList()
			)
		}
	}
}

data class PartyListsItemResponse(

	@field:SerializedName("last_partai_updated")
	val lastPartaiUpdated: String,

	@field:SerializedName("candidate_list")
	val candidateList: List<CandidateListItemResponse>,

	@field:SerializedName("partai_name")
	val partaiName: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("amount")
	val amount: Int
){
	fun toModel() = VoteData.PartyListsItem(
		candidateList = this.candidateList.map { it.toModel(this.id) },
		partyName = partaiName,
		id,
		totalPartyVote = amount,
		totalVote = amount + candidateList.sumOf { it.amount }
	)
}

data class CandidateListItemResponse(

	@field:SerializedName("no_urut")
	val noUrut: Int,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("amount")
	val amount: Int
){
	fun toModel(partyId: Int) = VoteData.Candidate(
		orderNumber = noUrut,
		candidateName = name,
		id = id,
		totalCandidateVote = amount,
		partyId = partyId
	)
}

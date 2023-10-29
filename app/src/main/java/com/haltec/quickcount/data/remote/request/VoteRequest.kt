package com.haltec.quickcount.data.remote.request

import com.google.gson.annotations.SerializedName
import com.haltec.quickcount.domain.model.VoteData

data class VoteRequest(
	@SerializedName("tps_id")
	val tpsId: Int,
	val partai: List<PartyItem>,
	@SerializedName("valid_vote")
	val validVote: Int,
	val amount: Int,
	val candidate: List<CandidateItem>,
	@SerializedName("selection_type_id")
	val selectionTypeId: Int,
	@SerializedName("invalid_vote")
	val invalidVote: Int
){
	data class PartyItem(
		val amount: Int,
		@SerializedName("partai_id")
		val partaiId: Int
	)

	data class CandidateItem(
		@SerializedName("candidate_id")
		val candidateId: Int,
		val amount: Int
	)
}


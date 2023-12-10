package com.haltec.quickcount.data.remote.response

import android.content.Context
import android.util.Log
import com.google.gson.annotations.SerializedName
import com.haltec.quickcount.data.local.entity.table.CandidateEntity
import com.haltec.quickcount.data.local.entity.table.ElectionEntity
import com.haltec.quickcount.data.local.entity.table.PartyEntity
import com.haltec.quickcount.data.local.entity.table.VoteFormEntity
import com.haltec.quickcount.util.capitalizeWords
import com.haltec.quickcount.util.stringToStringDateID
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.model.SubmitVoteStatus

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

		@field:SerializedName("active")
		val active: Int,

		@field:SerializedName("created_at")
		val createdAt: String,

		@field:SerializedName("id")
		val id: Int,

		@field:SerializedName("title")
		val title: String,

		@field:SerializedName("created_by")
		val createdBy: String,
		
		@field:SerializedName("tps_info")
		val tpsInfo: VoteFormResponse.VoteFormData,
		
		@field:SerializedName("attachment_list")
		val attachmentList: List<CurrentEvidenceResponse.CurrentEvidenceData>?
		
		
	){
		
		fun toEntity() = ElectionEntity(
			id = "${tpsInfo.tpsId}${id}".toInt(),
			updatedAt = updatedAt, 
			statusVote = statusVote, 
			active = active, 
			createdAt = createdAt, 
			electionId = id, 
			title = title,
			createdBy = createdBy,
			tpsId = tpsInfo.tpsId
		)
		
		fun toVoteFormEntity(): VoteFormEntity{
			return VoteFormEntity(
				tpsId = tpsInfo.tpsId,
				electionId = id,
				province = tpsInfo.province,
				subdistrict = tpsInfo.subdistrict,
				isPartai = tpsInfo.isPartai, village = tpsInfo.village,
				tpsName = tpsInfo.tpsName,
				amount = tpsInfo.amount, invalidVote = tpsInfo.invalidVote,
				note = tpsInfo.note,

				partaiList = this@ElectionResponse.toPartyEntity(tpsInfo.partaiLists)
			)
		}
		

		fun toPartyEntity(partaiLists: List<PartyListsItemResponse>?): List<PartyEntity>{
			return partaiLists?.map {
				PartyEntity(
					partaiName = it.partaiName,
					id = it.id,
					amount = it.amount,
					candidateList = this@ElectionResponse.toCandidateEntity(partyId = it.id)
				)
			} ?: emptyList()
		}

		fun toCandidateEntity(partyId: Int): List<CandidateEntity>{
			return tpsInfo.partaiLists?.filter { 
				it.id == partyId
			}?.flatMap {party ->
				party.candidateList.map {
					CandidateEntity(
						noUrut = it.noUrut,
						id = it.id,
						amount = it.amount,
						name = it.name
					)
				}
			} ?: emptyList()
		}
	}
	
	fun toModel() = this.data?.map { 
		Election(
			id = it.id,
			title = capitalizeWords(it.title),
			createdBy =  it.createdBy,
			createdAt = stringToStringDateID(it.createdAt),
			updatedAt = stringToStringDateID(it.updatedAt),
			statusVote = when(it.statusVote){
				SubmitVoteStatus.SUBMITTED.valueNumber -> SubmitVoteStatus.SUBMITTED
				SubmitVoteStatus.VERIFIED.valueNumber -> SubmitVoteStatus.VERIFIED
				SubmitVoteStatus.REJECTED.valueNumber -> SubmitVoteStatus.REJECTED
				else -> SubmitVoteStatus.PENDING
			},
			active = it.active
		)
	} ?: listOf()
}

fun List<ElectionListResponse.ElectionResponse>.toEntity(): List<ElectionEntity> = this.map { 
	return@map if (it.tpsInfo.tpsId != 0){
		it.toEntity()
	} else {
		null
	}
}.filterNotNull()

fun List<ElectionListResponse.ElectionResponse>.toVoteFormEntities() = this.map { 
	it.toVoteFormEntity()
}.filter { 
	it.tpsId != 0
}


suspend fun List<ElectionListResponse.ElectionResponse>.toUploadedEvidenceEntities(context: Context) = this.flatMap {
	it.attachmentList?.map { 
		it.toUploadedEvidenceEntity(context) 
	}?.filter { it.tpsId != 0 } ?: emptyList()
}

fun List<ElectionListResponse.ElectionResponse>.toUploadedEvidenceEntities() = this.flatMap {
	it.attachmentList?.map {
		it.toUploadedEvidenceEntity()
	}?.filter { it.tpsId != 0 } ?: emptyList()
}
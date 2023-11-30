package com.haltec.quickcount.data.remote.response

import android.content.Context
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
		val createdBy: String,
		
		@field:SerializedName("tps_info")
		val tpsInfo: VoteFormResponse.VoteFormData,
		
		@field:SerializedName("attachment_list")
		val attachmentList: List<CurrentEvidenceResponse.CurrentEvidenceData>?
		
		
	){
		fun toEntity(tpsId: Int) = ElectionEntity(
			updatedAt, statusVote, updatedBy, active, createdAt, id, title, createdBy, tpsId
		)
		
		fun toVoteFormEntity(): VoteFormEntity{
			this.apply { 
				tpsInfo.apply {
					return VoteFormEntity(
						tpsId = tpsId,
						electionId = id,
						province = province,
						subdistrict = subdistrict,
						isPartai = isPartai, village = village,
						tpsName = tpsName,
						amount = amount, invalidVote = invalidVote,
						note = note,

						partaiList = this@ElectionResponse.toPartyEntity()
					)
				}
			}
		}
		
		fun toPartyEntity(): List<PartyEntity>{
			this.apply { 
				return tpsInfo.partaiLists?.map {
					PartyEntity(
						partaiName = it.partaiName,
						id = it.id,
						amount = it.amount,
						candidateList = this@ElectionResponse.toCandidateEntity()
					)
				} ?: emptyList()
			}
		}

		fun toCandidateEntity(): List<CandidateEntity>{
			this.apply {
				return tpsInfo.partaiLists?.flatMap {party ->
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
		
		
	}
	
	fun toModel() = this.data?.map { 
		Election(
			id = it.id,
			title = capitalizeWords(it.title),
			createdBy =  it.createdBy,
			createdAt = stringToStringDateID(it.createdAt),
			updatedBy =  it.updatedBy,
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

fun List<ElectionListResponse.ElectionResponse>.toEntity(tpsId: Int) = this.map { 
	it.toEntity(tpsId)
}

fun List<ElectionListResponse.ElectionResponse>.toVoteFormEntities() = this.map { 
	it.toVoteFormEntity()
}.filter { 
	it.tpsId != 0
}

fun List<ElectionListResponse.ElectionResponse>.toPartyEntity() = this.flatMap {
	it.toPartyEntity()
}

fun List<ElectionListResponse.ElectionResponse>.toCandidateEntity() = this.flatMap {
	it.toCandidateEntity()
}

suspend fun List<ElectionListResponse.ElectionResponse>.toUploadedEvidenceEntities(context: Context) = this.flatMap {
	it.attachmentList?.map { 
		it.toUploadedEvidenceEntity(context) 
	} ?: emptyList()
}

fun List<ElectionListResponse.ElectionResponse>.toUploadedEvidenceEntities() = this.flatMap {
	it.attachmentList?.map {
		it.toUploadedEvidenceEntity()
	} ?: emptyList()
}
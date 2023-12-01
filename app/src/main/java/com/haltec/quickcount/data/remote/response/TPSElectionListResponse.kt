package com.haltec.quickcount.data.remote.response

import com.google.gson.annotations.SerializedName
import com.haltec.quickcount.data.local.entity.table.CandidateEntity
import com.haltec.quickcount.data.local.entity.table.ElectionEntity
import com.haltec.quickcount.data.local.entity.table.PartyEntity
import com.haltec.quickcount.data.local.entity.table.TPSEntity
import com.haltec.quickcount.data.local.entity.table.UploadedEvidenceEntity
import com.haltec.quickcount.data.local.entity.table.VoteFormEntity
import com.haltec.quickcount.util.capitalizeWords
import com.haltec.quickcount.util.stringToStringDateID
import com.haltec.quickcount.domain.model.TPSElection
import com.haltec.quickcount.domain.model.stringToSubmitVoteStatus

data class TPSElectionListResponse(
	@field:SerializedName("data")
	val data: List<TPSElectionResponse>? = null,

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("version")
	val version: String
){
	data class TPSElectionResponse(

		@field:SerializedName("village_code")
		val villageCode: String,

		@field:SerializedName("address")
		val address: String,

		@field:SerializedName("city")
		val city: String,

		@field:SerializedName("latitude")
		val latitude: String,

		@field:SerializedName("dpt")
		val dpt: Int,

		@field:SerializedName("created_at")
		val createdAt: String,

		@field:SerializedName("created_by")
		val createdBy: String,

		@field:SerializedName("selection_type")
		val selectionType: String,

		@field:SerializedName("subdistrict")
		val subdistrict: String,

		@field:SerializedName("provice")
		val province: String,

		@field:SerializedName("selection_type_id")
		val selectionTypeId: Int,

		@field:SerializedName("name")
		val name: String,

		@field:SerializedName("id")
		val id: Int,

		@field:SerializedName("village")
		val village: String,

		@field:SerializedName("longitude")
		val longitude: String,

		@field:SerializedName("status")
		val status: String,

		@field:SerializedName("submited")
		val submitted: String,

		@field:SerializedName("rejected")
		val rejected: String,

		@field:SerializedName("approved")
		val approved: String,

		@field:SerializedName("pending")
		val pending: String,

		@field:SerializedName("tps_info")
		val tpsInfo: VoteFormResponse.VoteFormData,

		@field:SerializedName("attachment_list")
		val attachmentList: List<CurrentEvidenceResponse.CurrentEvidenceData>
	){
		fun toModel() =
			TPSElection(
				tpsId = id,
				tpsName = name,
				electionId = selectionTypeId,
				electionName = selectionType,
				village = village,
				villageCode = villageCode,
				subdistrict = subdistrict,
				city = city,
				province = province,
				longitude = longitude,
				latitude = latitude,
				createdAt = stringToStringDateID(createdAt),
				statusVote = stringToSubmitVoteStatus(status),
				createdBy = capitalizeWords(createdBy),
				address = address,
				dpt = dpt
			)
		
		fun toTPSEntity() = TPSEntity(
			villageCode = villageCode,
			address = address,
			city = city,
			latitude = latitude,
			createdAt = createdAt,
			id = id,
			dpt = dpt,
			longitude = longitude,
			village = village,
			approved = approved,
			rejected = rejected,
			pending = pending,
			submitted = submitted,
			createdBy = createdBy,
			name = name,
			province = province,
			subdistrict = subdistrict
		)
		
		fun toElectionEntity() = ElectionEntity(
			id = "${id}$selectionTypeId".toInt(),
			updatedBy = "",
			updatedAt = "",
			createdBy = "",
			createdAt = "",
			active = 0,
			statusVote = status,
			tpsId = id,
			electionId = selectionTypeId,
			title = selectionType
		)

		fun toVoteFormEntity(): VoteFormEntity {
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

						partaiList = this@TPSElectionResponse.toPartyEntity()
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
						candidateList = this@TPSElectionResponse.toCandidateEntity()
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
	
	fun toModel() = this.data?.map { it.toModel() }
	
}

fun List<TPSElectionListResponse.TPSElectionResponse>.toTPSEntities() = this.map {
	it.toTPSEntity()
}.filter { it.id != 0 }.distinct()

fun List<TPSElectionListResponse.TPSElectionResponse>.toElectionEntities(): List<ElectionEntity> = this.map {
	return@map if (it.tpsInfo.tpsId != 0){
		it.toElectionEntity()
	}else{
		null
	}
}.filterNotNull()

fun List<TPSElectionListResponse.TPSElectionResponse>.toVoteFormEntities(): List<VoteFormEntity> = this.map {
	return@map if (it.tpsInfo.tpsId != 0){
		it.toVoteFormEntity()
	}else{
		null
	}
}.filterNotNull()

fun List<TPSElectionListResponse.TPSElectionResponse>.toUploadedEvidenceEntities(): List<UploadedEvidenceEntity> = this.flatMap {
	it.attachmentList.map {
		it.toUploadedEvidenceEntity()
	}
}.filter { it.tpsId != 0 }

package com.haltec.quickcount.data.local.entity.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverters
import com.haltec.quickcount.data.local.entity.typeConverter.TempVoteCandidateListConverter
import com.haltec.quickcount.data.local.entity.typeConverter.TempVotePartyListConverter
import com.haltec.quickcount.data.remote.request.VoteRequest

const val TEMP_VOTE_SUBMIT_ENTITY = "temp_vote_submit_entity"
@Entity(tableName = TEMP_VOTE_SUBMIT_ENTITY, primaryKeys = ["tps_id", "election_id"])
data class TempVoteSubmitEntity(
    @ColumnInfo("tps_id")
    val tpsId: Int,

    @ColumnInfo("election_id")
    val electionId: Int,

    @ColumnInfo("valid_vote")
    val validVote: Int,

    @ColumnInfo("invalid_vote")
    val invalidVote: Int,

    val amount: Int,

    @ColumnInfo("is_partai")
    val isPartai: Int,

    
    val partai: List<TempVotePartyEntity>? = null,
    
    val candidate: List<TempVoteCandidateEntity>? = null
){
    fun toRequest(): VoteRequest {
        return VoteRequest(
            tpsId = tpsId,
            selectionTypeId = electionId,
            invalidVote = invalidVote,
            validVote = validVote,
            amount = amount,
            isPartai = isPartai,
            candidate = candidate?.map { 
                VoteRequest.CandidateItem(
                    candidateId = it.candidateId,
                    amount = amount
                )
            } ?: emptyList(),
            partai = partai?.map { 
                VoteRequest.PartyItem(
                    partaiId = it.partaiId,
                    amount = it.amount
                )
            }
        )
    }
}

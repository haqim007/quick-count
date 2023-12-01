package com.haltec.quickcount.data.local.entity.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.haltec.quickcount.domain.model.VoteData

const val VOTE_FORM_TABLE = "vote_form"
@Entity(
    tableName = VOTE_FORM_TABLE,
)
data class VoteFormEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo("tps_id")
    val tpsId: Int,
    @ColumnInfo("election_id")
    val electionId: Int,
    val province: String,
    val subdistrict: String,
    @ColumnInfo("is_partai")
    val isPartai: Int,
    @ColumnInfo("village")
    val village: String,
    @ColumnInfo("tps_name")
    val tpsName: String,
    @ColumnInfo("amount")
    val amount: Int,
    @ColumnInfo("invalid_vote")
    val invalidVote: Int,
    @ColumnInfo("note")
    val note: String?,
    
    @ColumnInfo("partai_list")
    val partaiList: List<PartyEntity>
){
    fun toModel(city: String): VoteData{
        val partyLists = partaiList.mapIndexed { index, it ->
            VoteData.PartyListsItem(
                partyName = it.partaiName,
                id = it.id,
                totalPartyVote = it.amount,
                totalVote = it.amount + it.candidateList.sumOf { it.amount },
                isExpanded = index == 0,
                candidateList = it.candidateList.map { candidate -> 
                    VoteData.Candidate(
                        orderNumber = candidate.noUrut,
                        candidateName = candidate.name,
                        id = candidate.id,
                        totalCandidateVote = candidate.amount,
                        partyId = it.id
                    )
                }
            )
        }
        return VoteData(
            tpsId = tpsId,
            province = province,
            subdistrict = subdistrict,
            partyLists = partyLists,
            village = village,
            city = city,
            tpsName = tpsName,
            validVote = amount,
            invalidVote = invalidVote,
            note = note,
            isParty = isPartai == 1
        )
    }
}

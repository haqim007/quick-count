package com.haltec.quickcount.data.local.entity.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.haltec.quickcount.util.capitalizeWords
import com.haltec.quickcount.util.stringToStringDateID
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.model.SubmitVoteStatus

const val ELECTION_TABLE = "election"
@Entity(tableName = ELECTION_TABLE)
data class ElectionEntity(
    @ColumnInfo("updated_at")
    val updatedAt: String,
    @ColumnInfo("status_vote")
    val statusVote: String,
    @ColumnInfo("updated_by")
    val updatedBy: String,
    val active: Int,
    @ColumnInfo("created_at")
    val createdAt: String,
    @PrimaryKey
    val id: Int,
    val title: String,
    @ColumnInfo("created_by")
    val createdBy: String,
    @ColumnInfo("tps_id")
    val tpsId: Int
){
    
    fun toModel() = Election(
        id = this.id,
        title = capitalizeWords(this.title),
        createdBy =  this.createdBy,
        createdAt = stringToStringDateID(this.createdAt),
        updatedBy =  this.updatedBy,
        updatedAt = stringToStringDateID(this.updatedAt),
        statusVote = when(this.statusVote){
            SubmitVoteStatus.SUBMITTED.valueNumber -> SubmitVoteStatus.SUBMITTED
            SubmitVoteStatus.VERIFIED.valueNumber -> SubmitVoteStatus.VERIFIED
            SubmitVoteStatus.REJECTED.valueNumber -> SubmitVoteStatus.REJECTED
            SubmitVoteStatus.IN_QUEUE.valueNumber -> SubmitVoteStatus.IN_QUEUE
            else -> SubmitVoteStatus.PENDING
        },
        active = this.active
    )
}

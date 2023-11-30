package com.haltec.quickcount.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.haltec.quickcount.data.local.entity.table.PartyEntity
import com.haltec.quickcount.data.local.entity.table.TEMP_VOTE_SUBMIT_ENTITY
import com.haltec.quickcount.data.local.entity.table.TempVoteSubmitEntity
import com.haltec.quickcount.data.local.entity.table.VOTE_FORM_TABLE
import com.haltec.quickcount.data.local.entity.table.VoteFormEntity

@Dao
interface VoteFormDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(voteForm: List<VoteFormEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(voteForm: VoteFormEntity)

    @Transaction
    @Query("SELECT * FROM $VOTE_FORM_TABLE where tps_id = :tpsId AND election_id = :electionId")
    suspend fun getVoteForm(tpsId: Int, electionId: Int): VoteFormEntity? //FullVoteDataEntity?

    @Query("SELECT * FROM $VOTE_FORM_TABLE where id = :id")
    suspend fun getById(id: Int): VoteFormEntity?

    @Query("DELETE FROM $VOTE_FORM_TABLE")
    suspend fun clearAll()

    @Query("DELETE FROM $VOTE_FORM_TABLE WHERE tps_id NOT IN (:tpsIds)")
    suspend fun clearAllByTps(tpsIds: List<Int>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTempVoteSubmit(tempVoteData: TempVoteSubmitEntity)

    @Query("DELETE FROM $TEMP_VOTE_SUBMIT_ENTITY WHERE tps_id = :tpsId AND election_id = :electionId")
    suspend fun removeTempVoteSubmit(tpsId: Int, electionId: Int)
    
    @Transaction
    suspend fun insertOrReplaceTempVoteSubmit(tempVoteData: TempVoteSubmitEntity){
        removeTempVoteSubmit(tempVoteData.tpsId, tempVoteData.electionId)
        insertTempVoteSubmit(tempVoteData)
    }


    @Query("UPDATE $VOTE_FORM_TABLE SET partai_list = :partyList WHERE id = :electionId AND tps_id = :tpsId")
    suspend fun updateVoteForm(
        tpsId: Int, 
        electionId: Int, 
        partyList: List<PartyEntity>,
    )

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateVoteForm(
        voteForm: VoteFormEntity
    )

    @Transaction
    suspend fun updateVoteForm(
        tpsId: Int,
        electionId: Int,
        partyList: List<PartyEntity>,
        invalidVote: Int,
        validVote: Int
    ){
        val existingData = getVoteForm(tpsId, electionId)
        existingData?.let {
            updateVoteForm(
                it.copy(
                    partaiList = partyList,
                    invalidVote = invalidVote,
                    amount = validVote
                )
            )
        }
    }
    
    @Query("SELECT * FROM $TEMP_VOTE_SUBMIT_ENTITY")
    suspend fun getTempVoteData(): List<TempVoteSubmitEntity>

}
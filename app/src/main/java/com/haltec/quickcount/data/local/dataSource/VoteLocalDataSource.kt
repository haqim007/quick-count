package com.haltec.quickcount.data.local.dataSource

import androidx.room.withTransaction
import com.haltec.quickcount.data.local.entity.table.TempVoteSubmitEntity
import com.haltec.quickcount.data.local.entity.table.VoteFormEntity
import com.haltec.quickcount.data.local.room.AppDatabase
import com.haltec.quickcount.domain.model.SubmitVoteStatus
import javax.inject.Inject

/**
 * Vote local data source
 *
 * @property database
 * @constructor 
 */

class VoteLocalDataSource @Inject constructor(
    private val database: AppDatabase
) {

    suspend fun getVoteForm(tpsId: Int, electionId: Int): VoteFormEntity? //FullVoteDataEntity? 
    {
        return database.voteFormDao().getVoteForm(tpsId, electionId)
    }
    
    suspend fun insertOrReplace(
        voteForm: VoteFormEntity
    ){
        database.withTransaction {
            val existingData = database.voteFormDao().getVoteForm(voteForm.tpsId, voteForm.electionId)
            database.voteFormDao().insert(
                voteForm.copy(id = existingData?.id ?: voteForm.id) // replace the id with existing data to trigger update if there any difference or insert if not exist
            )
        }
    }

    suspend fun insertAll(
        voteForm: List<VoteFormEntity>,
        tpsIds: List<Int>,
        electionId: List<Int>
    ){
        database.withTransaction {
            database.voteFormDao().clearAllByTpsElections(tpsIds, electionId)
            database.voteFormDao().insertAll(voteForm)
        }
    }

    suspend fun insertAll(
        voteForm: List<VoteFormEntity>
    ){
        database.voteFormDao().insertAll(voteForm)
    }
    suspend fun removeTempVoteSubmit(tpsId: Int, electionId: Int){
        database.voteFormDao().removeTempVoteSubmit(tpsId, electionId)
    }
    
    suspend fun onVoteSubmitSuccess(tpsId: Int, electionId: Int){
        database.withTransaction {
            // remove vote data from temp_vote_submit table
            removeTempVoteSubmit(tpsId, electionId)
            // decrease total of queue of waiting data to be sent
            database.tpsDao().decreaseTotalWaitToBeSentData(tpsId)
            // increase total of submitted vote
            database.tpsDao().increaseTotalVoteSubmitted(tpsId)
            // update election status as submitted
            database.electionDao().updateStatus(tpsId, electionId, SubmitVoteStatus.SUBMITTED.valueNumber)
        }
    }
    
    suspend fun insertOrReplaceTempVoteData(
        tempVoteData: TempVoteSubmitEntity
    ){
        database.withTransaction {
            // insert to queue table
            database.voteFormDao().insertOrReplaceTempVoteSubmit(tempVoteData)
            // update the status
            database.electionDao().updateStatus(tempVoteData.tpsId, tempVoteData.electionId, SubmitVoteStatus.IN_QUEUE.valueNumber)
            // increase total of data wait to be sent in tps entity
            database.tpsDao().increaseTotalWaitToBeSentData(tempVoteData.tpsId)
            
            // update the form
            val currentVoteForm = database.voteFormDao().getVoteForm(tempVoteData.tpsId, tempVoteData.electionId)
            currentVoteForm?.let { 
                val partyList = it.partaiList.map { party ->
                    val matchingParty = tempVoteData.partai?.firstOrNull { it.partaiId == party.id }

                    matchingParty?.let { matchingPartyItem ->
                        party.copy(
                            amount = matchingPartyItem.amount,
                            candidateList = party.candidateList.map { candidate ->
                                val matchingCandidate = tempVoteData.candidate?.firstOrNull { it.candidateId == candidate.id }
                                matchingCandidate?.let {
                                    candidate.copy(
                                        amount = it.amount
                                    )
                                } ?: candidate
                            }
                        )
                    } ?: run { 
                        if(party.id == 0){
                            // update non-partai candidate
                            party.copy(
                                candidateList = party.candidateList.map { candidate ->
                                    val matchingCandidate = tempVoteData.candidate?.firstOrNull { it.candidateId == candidate.id }
                                    matchingCandidate?.let {
                                        candidate.copy(
                                            amount = it.amount
                                        )
                                    } ?: candidate
                                }
                            )
                        }else{
                            party
                        }
                    }
                }
                
                database.voteFormDao().updateVoteForm(
                    tempVoteData.tpsId,
                    tempVoteData.electionId, 
                    partyList,
                    tempVoteData.invalidVote,
                    tempVoteData.validVote 
                )
            }
            
        }
        
    }

    suspend fun getTempVoteData(): List<TempVoteSubmitEntity> {
        return database.voteFormDao().getTempVoteData()
    }

}
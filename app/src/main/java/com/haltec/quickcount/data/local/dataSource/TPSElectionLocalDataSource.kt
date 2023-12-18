package com.haltec.quickcount.data.local.dataSource

import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.haltec.quickcount.data.local.entity.table.ELECTION_TABLE
import com.haltec.quickcount.data.local.entity.table.ElectionEntity
import com.haltec.quickcount.data.local.entity.table.RemoteKeys
import com.haltec.quickcount.data.local.entity.table.TPSEntity
import com.haltec.quickcount.data.local.entity.table.TPS_TABLE
import com.haltec.quickcount.data.local.entity.table.UploadedEvidenceEntity
import com.haltec.quickcount.data.local.entity.table.VoteFormEntity
import com.haltec.quickcount.data.local.entity.view.TPSElectionEntity
import com.haltec.quickcount.data.local.entity.view.TPS_ELECTION_VIEW
import com.haltec.quickcount.data.local.room.AppDatabase
import com.haltec.quickcount.domain.model.SubmitVoteStatus
import javax.inject.Inject

class TPSElectionLocalDataSource @Inject constructor(
    private val database: AppDatabase,
    private val tpsLocalDataSource: TPSLocalDataSource,
    private val electionLocalDataSource: ElectionLocalDataSource,
    private val voteLocalDataSource: VoteLocalDataSource,
    private val uploadLocalDataSource: UploadEvidenceLocalDataSource
) {

    suspend fun syncInsertAll(
        tps: List<TPSEntity>,
        election: List<ElectionEntity>,
        voteFrom: List<VoteFormEntity>,
        uploadedEvidence: List<UploadedEvidenceEntity>
    ){
        database.withTransaction {
            
            //reset current data
            database.remoteKeysDao().clearRemoteKeys(TPS_ELECTION_VIEW)
            database.remoteKeysDao().clearRemoteKeys(ELECTION_TABLE)
            database.voteFormDao().clearAll()
            database.uploadEvidenceDao().clearAll()
            database.electionDao().clearAll()
            database.remoteKeysDao().clearRemoteKeys(TPS_TABLE)
            database.tpsDao().clearAll()
            // end of resetting

            val tpsRemoteKeys = tps.map {
                RemoteKeys(
                    tableId = it.id,
                    tableName = TPS_TABLE,
                    prevKey = null,
                    nextKey = null
                )
            }
            
            database.remoteKeysDao().insertAll(tpsRemoteKeys)
            
            val existingInQueueVoteTPSIds: MutableList<Int> = mutableListOf()
            val existingInQueueVoteElectionIds: MutableList<Int> = mutableListOf()
            database.voteFormDao().getTempVoteData().forEach {
                existingInQueueVoteTPSIds.add(it.tpsId)
                existingInQueueVoteElectionIds.add(it.electionId)
            }
            // update total of waitToBeSent
            val newTps = tps.map {tpsEntity -> 
                if (existingInQueueVoteTPSIds.contains(tpsEntity.id)){
                    tpsEntity.copy(
                        waitToBeSent = existingInQueueVoteTPSIds.count { it == tpsEntity.id }.toString()
                    )
                }else{
                    tpsEntity
                }
            }
            database.tpsDao().insertAll(newTps)

            val electionRemoteKeys = election.map {
                RemoteKeys(
                    tableId = it.id,
                    tableName = ELECTION_TABLE,
                    prevKey = null,
                    nextKey = null
                )
            }

            database.remoteKeysDao().insertAll(electionRemoteKeys)
            
            // change status to in queue when in_queue submit vote exists
            val newElection = election.map { electionEntity -> 
                if (
                    existingInQueueVoteElectionIds.contains(electionEntity.electionId) && 
                    existingInQueueVoteTPSIds.contains(electionEntity.tpsId)
                ){
                    electionEntity.copy(
                        statusVote = SubmitVoteStatus.IN_QUEUE.valueNumber
                    )    
                }else{
                    electionEntity
                }
            }
            database.electionDao().insertAll(newElection, false)
        }
        
        voteLocalDataSource.insertAll(
            voteFrom, 
            tpsIds = tps.map { it.id },
            electionId = election.map { it.electionId }
        )
        uploadLocalDataSource.insertUploadedEvidence(
            uploadedEvidence,
            tpsIds = tps.map { it.id },
            electionIds = election.map { it.electionId }
        )
    }
    
    suspend fun insertAll(
        tps: List<TPSEntity>, 
        election: List<ElectionEntity>, 
        voteFrom: List<VoteFormEntity>,
        remoteKeys: List<RemoteKeys>,
        uploadedEvidence: List<UploadedEvidenceEntity>,
        isRefresh: Boolean = false,
        filter: String? = null
    ){
        database.withTransaction {

            if (isRefresh){
                database.remoteKeysDao().clearRemoteKeys(TPS_ELECTION_VIEW)
                database.remoteKeysDao().clearRemoteKeys(ELECTION_TABLE)
                
                // if filter null means refresh all elections and tps
                // else only refresh elections by filter value
                if(filter != null){
                    database.electionDao().clearByFilter(filter)
                }else{
                    database.voteFormDao().clearAll()
                    database.remoteKeysDao().clearRemoteKeys(TPS_TABLE)
                    database.electionDao().clearAll()
                    database.tpsDao().clearAll()
                }
            }
            
            database.remoteKeysDao().insertAll(remoteKeys)

            val tpsRemoteKeys = tps.map {
                RemoteKeys(
                    tableId = it.id,
                    tableName = TPS_TABLE,
                    prevKey = null,
                    nextKey = null
                )
            }
            
            database.remoteKeysDao().insertAll(tpsRemoteKeys)
            val existingInQueueVoteTPSIds: MutableList<Int> = mutableListOf()
            val existingInQueueVoteElectionIds: MutableList<Int> = mutableListOf()
            database.voteFormDao().getTempVoteData().forEach {
                existingInQueueVoteTPSIds.add(it.tpsId)
                existingInQueueVoteElectionIds.add(it.electionId)
            }
            // update total of waitToBeSent
            val newTps = tps.map {tpsEntity ->
                if (existingInQueueVoteTPSIds.contains(tpsEntity.id)){
                    tpsEntity.copy(
                        waitToBeSent = existingInQueueVoteTPSIds.count { it == tpsEntity.id }.toString()
                    )
                }else{
                    tpsEntity
                }
            }
            database.tpsDao().insertAll(newTps)

            val electionRemoteKeys = election.map {
                RemoteKeys(
                    tableId = it.id,
                    tableName = ELECTION_TABLE,
                    prevKey = null,
                    nextKey = null
                )
            }
           
            database.remoteKeysDao().insertAll(electionRemoteKeys)
            // change status to in queue when in_queue submit vote exists
            val newElection = election.map { electionEntity ->
                if (
                    existingInQueueVoteElectionIds.contains(electionEntity.electionId) &&
                    existingInQueueVoteTPSIds.contains(electionEntity.tpsId)
                ){
                    electionEntity.copy(
                        statusVote = SubmitVoteStatus.IN_QUEUE.valueNumber
                    )
                }else{
                    electionEntity
                }
            }
            database.electionDao().insertAll(newElection, true)
        }

        voteLocalDataSource.insertAll(voteFrom)
        uploadLocalDataSource.insertUploadedEvidence(uploadedEvidence)
    }

    fun getPaging(filter: String? = null): PagingSource<Int, TPSElectionEntity> {
        return filter?.let { 
            database.tpsElectionDao().getPaging(it) 
        } ?: database.tpsElectionDao().getPaging()
    }

    suspend fun getRemoteKeyById(id: Int): RemoteKeys? {
        return database.remoteKeysDao().getRemoteKeyById(id, TPS_ELECTION_VIEW)
    }
    
}
package com.haltec.quickcount.data.local.dataSource

import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.haltec.quickcount.data.local.entity.table.ELECTION_TABLE
import com.haltec.quickcount.data.local.entity.table.ElectionEntity
import com.haltec.quickcount.data.local.entity.table.RemoteKeys
import com.haltec.quickcount.data.local.room.AppDatabase
import com.haltec.quickcount.domain.model.SubmitVoteStatus
import javax.inject.Inject

/**
 * Election local data source
 *
 * @property database
 * @constructor Create empty T p s local data source
 */

class ElectionLocalDataSource @Inject constructor(
    private val database: AppDatabase
) {
    
    suspend fun getRemoteKeyById(id: Int): RemoteKeys? {
        return database.remoteKeysDao().getRemoteKeyById(id, ELECTION_TABLE)
    }
    
    fun getPaging(tpsId: Int): PagingSource<Int, ElectionEntity>{
        return database.electionDao().getPaging(tpsId)
    }

    suspend fun insertAllAndRemoteKeys(
        remoteKeys: List<RemoteKeys>,
        election: List<ElectionEntity>,
        isRefresh: Boolean = false,
        isFromTPSElection: Boolean = false,
        tpsId: Int
    ){
        database.withTransaction { 
            if (isRefresh){
                database.remoteKeysDao().clearRemoteKeys(ELECTION_TABLE)
                database.electionDao().clearAll(tpsId)
            }
            database.remoteKeysDao().insertAll(remoteKeys)

            val existingInQueueVoteTPSIds: MutableList<Int> = mutableListOf()
            val existingInQueueVoteElectionIds: MutableList<Int> = mutableListOf()
            database.voteFormDao().getTempVoteData().forEach {
                existingInQueueVoteTPSIds.add(it.tpsId)
                existingInQueueVoteElectionIds.add(it.electionId)
            }
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
            database.electionDao().insertAll(newElection, isFromTPSElection)
        }
    }
}
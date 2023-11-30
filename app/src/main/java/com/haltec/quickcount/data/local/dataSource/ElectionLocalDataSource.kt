package com.haltec.quickcount.data.local.dataSource

import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.haltec.quickcount.data.local.entity.table.ELECTION_TABLE
import com.haltec.quickcount.data.local.entity.table.ElectionEntity
import com.haltec.quickcount.data.local.entity.table.RemoteKeys
import com.haltec.quickcount.data.local.entity.table.UploadedEvidenceEntity
import com.haltec.quickcount.data.local.entity.table.VoteFormEntity
import com.haltec.quickcount.data.local.room.AppDatabase
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

    suspend fun getById(id: Int): ElectionEntity?{
        return database.electionDao().getById(id)
    }

    suspend fun clearAll(){
        database.electionDao().clearAll()
    }
    
    suspend fun insertAllAndRemoteKeys(
        remoteKeys: List<RemoteKeys>, 
        election: List<ElectionEntity>,
        voteForm: List<VoteFormEntity>,
        uploadedEvidence: List<UploadedEvidenceEntity>,
        isRefresh: Boolean = false
    ){
        database.withTransaction { 
            if (isRefresh){
                database.remoteKeysDao().clearRemoteKeys(ELECTION_TABLE)
                database.electionDao().clearAll()
            }
            database.remoteKeysDao().insertAll(remoteKeys)
            database.electionDao().insertAll(election)
            val electionIds = election.map { it.id }
            database.voteFormDao().insertAll(voteForm)
            database.uploadEvidenceDao().insertUploadEvidence(uploadedEvidence)
        }
    }
}
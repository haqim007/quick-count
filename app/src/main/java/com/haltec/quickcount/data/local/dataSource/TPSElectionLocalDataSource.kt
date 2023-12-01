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

            val tpsRemoteKeys = tps.map {
                RemoteKeys(
                    tableId = it.id,
                    tableName = TPS_TABLE,
                    prevKey = null,
                    nextKey = null
                )
            }

            tpsLocalDataSource.insertAllAndRemoteKeys(remoteKeys = tpsRemoteKeys, tps, true)

            val electionRemoteKeys = election.map {
                RemoteKeys(
                    tableId = it.id,
                    tableName = ELECTION_TABLE,
                    prevKey = null,
                    nextKey = null
                )
            }


            database.remoteKeysDao().clearRemoteKeys(ELECTION_TABLE)
            database.voteFormDao().clearAll()
            database.uploadEvidenceDao().clearAll()
            database.electionDao().clearAll()
            database.remoteKeysDao().insertAll(electionRemoteKeys)
            database.electionDao().insertAll(election, false)
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
        isRefresh: Boolean = false
    ){
        database.withTransaction {

            if (isRefresh){
                database.remoteKeysDao().clearRemoteKeys(TPS_ELECTION_VIEW)
                database.remoteKeysDao().clearRemoteKeys(TPS_TABLE)
                database.remoteKeysDao().clearRemoteKeys(ELECTION_TABLE)
                database.electionDao().clearAll()
                database.tpsDao().clearAll()
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
            
            tpsLocalDataSource.insertAllAndRemoteKeys(remoteKeys = tpsRemoteKeys, tps, isRefresh)

            val electionRemoteKeys = election.map {
                RemoteKeys(
                    tableId = it.id,
                    tableName = ELECTION_TABLE,
                    prevKey = null,
                    nextKey = null
                )
            }
           
            database.remoteKeysDao().insertAll(electionRemoteKeys)
            database.electionDao().insertAll(election, true)
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
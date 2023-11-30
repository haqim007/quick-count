package com.haltec.quickcount.data.local.dataSource

import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.haltec.quickcount.data.local.entity.table.RemoteKeys
import com.haltec.quickcount.data.local.entity.table.TPSEntity
import com.haltec.quickcount.data.local.entity.table.TPS_TABLE
import com.haltec.quickcount.data.local.room.AppDatabase
import javax.inject.Inject

/**
 * TPS local data source
 *
 * @property database
 * @constructor Create empty T p s local data source
 */

class TPSLocalDataSource @Inject constructor(
    private val database: AppDatabase
) {
    
    suspend fun getRemoteKeyById(id: Int): RemoteKeys? {
        return database.remoteKeysDao().getRemoteKeyById(id, TPS_TABLE)
    }
    
    fun getPaging(): PagingSource<Int, TPSEntity>{
        return database.tpsDao().getPaging()
    }

    suspend fun getById(id: Int): TPSEntity?{
        return database.tpsDao().getById(id)
    }

    suspend fun clearAll(){
        database.tpsDao().clearAll()
    }
    
    suspend fun insertAllAndRemoteKeys(
        remoteKeys: List<RemoteKeys>, 
        tps: List<TPSEntity>,
        isRefresh: Boolean = false
    ){
        database.withTransaction { 
            if (isRefresh){
                database.remoteKeysDao().clearRemoteKeys(TPS_TABLE)
                database.tpsDao().clearAll()
            }
            database.remoteKeysDao().insertAll(remoteKeys)
            database.tpsDao().insertAll(tps)
            val tpsIds = tps.map { it.id }
            // clean election entity to prevent foreign key constraint failed
            database.electionDao().clearAll(tpsIds)
            // clean vote_form entity to prevent foreign key constraint failed
            database.voteFormDao().clearAllByTps(tpsIds)
            // clean uploaded_evidence entity to prevent foreign key constraint failed
            database.uploadEvidenceDao().clearAllByTps(tpsIds)
        }
    }
    
    suspend fun decreaseTotalVoteToBeSent(tpsId: Int){
        database.tpsDao().decreaseTotalWaitToBeSentData(tpsId)
    }
}
package com.haltec.quickcount.data.pagingsource

import androidx.paging.LoadType
import com.haltec.quickcount.data.local.dataSource.TPSElectionLocalDataSource
import com.haltec.quickcount.data.local.entity.table.RemoteKeys
import com.haltec.quickcount.data.local.entity.view.TPSElectionEntity
import com.haltec.quickcount.data.local.entity.view.TPS_ELECTION_VIEW
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.base.checkToken
import com.haltec.quickcount.data.remote.datasource.TPSElectionRemoteDataSource
import com.haltec.quickcount.data.remote.response.toElectionEntities
import com.haltec.quickcount.data.remote.response.toTPSEntities
import com.haltec.quickcount.data.remote.response.toUploadedEvidenceEntities
import com.haltec.quickcount.data.remote.response.toVoteFormEntities

class TPSElectionRemoteMediator (
    private val userPreference: UserPreference,
    private val localDataSource: TPSElectionLocalDataSource,
    private val remoteDataSource: TPSElectionRemoteDataSource,
    private val filter: String = "",
    isOnline: suspend() -> Boolean = {true}
): BaseRemoteMediator<TPSElectionEntity>(
    userPreference, isOnline
){
    
    override suspend fun processData(page: Int, loadType: LoadType): Boolean{
        // val offset = (page - 1) * state.config.pageSize
        val response = checkToken(userPreference) { remoteDataSource.getTPSElectionList(filter) }
        val endOfPaginationReached = true
        val prevKey = null //if(page == 1) null else page - 1
        val nextKey = null //if (endOfPaginationReached) null else page + 1
        val tpsElectionList = response.getOrThrow().data ?: listOf()
        val remoteKeys = tpsElectionList.map {
            RemoteKeys(
                tableId = it.id,
                tableName = TPS_ELECTION_VIEW,
                prevKey = prevKey,
                nextKey = nextKey
            )
        }

        localDataSource.insertAll(
            remoteKeys = remoteKeys,
            tps = tpsElectionList.toTPSEntities(),
            election = tpsElectionList.toElectionEntities(),
            voteFrom = tpsElectionList.toVoteFormEntities(),
            uploadedEvidence = tpsElectionList.toUploadedEvidenceEntities(),
            isRefresh = loadType == LoadType.REFRESH && filter.isEmpty()
        )
        
        return endOfPaginationReached
    }

    override suspend fun getRemoteKeyById(data: TPSElectionEntity): RemoteKeys? {
        return localDataSource.getRemoteKeyById(data.electionId)
    }
}

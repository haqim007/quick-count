package com.haltec.quickcount.data.pagingsource

import androidx.paging.LoadType
import com.haltec.quickcount.data.local.dataSource.ElectionLocalDataSource
import com.haltec.quickcount.data.local.dataSource.UploadEvidenceLocalDataSource
import com.haltec.quickcount.data.local.dataSource.VoteLocalDataSource
import com.haltec.quickcount.data.local.entity.table.ELECTION_TABLE
import com.haltec.quickcount.data.local.entity.table.ElectionEntity
import com.haltec.quickcount.data.local.entity.table.RemoteKeys
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.base.checkToken
import com.haltec.quickcount.data.remote.datasource.ElectionRemoteDataSource
import com.haltec.quickcount.data.remote.response.toEntity
import com.haltec.quickcount.data.remote.response.toUploadedEvidenceEntities
import com.haltec.quickcount.data.remote.response.toVoteFormEntities


class ElectionRemoteMediator(
    private val userPreference: UserPreference,
    private val localDataSource: ElectionLocalDataSource,
    private val remoteDataSource: ElectionRemoteDataSource,
    private val voteLocalDataSource: VoteLocalDataSource,
    private val uploadEvidenceLocalDataSource: UploadEvidenceLocalDataSource,
    private val tpsId: Int,
    isOnline: suspend() -> Boolean = {true}
): BaseRemoteMediator<ElectionEntity>(userPreference, isOnline){

    override suspend fun processData(page: Int, loadType: LoadType): Boolean {
        // val offset = (page - 1) * state.config.pageSize
        val response = checkToken(userPreference) { remoteDataSource.getElectionList(tpsId) }
        val endOfPaginationReached = true
        val prevKey = null //if(page == 1) null else page - 1
        val nextKey = null //if (endOfPaginationReached) null else page + 1
        val electionList = response.getOrThrow().data ?: listOf()
        val remoteKeys = electionList.map {
            RemoteKeys(
                tableId = "${it.tpsInfo.tpsId}${it.id}".toInt(),
                tableName = ELECTION_TABLE,
                prevKey = prevKey,
                nextKey = nextKey
            )
        }

        localDataSource.insertAllAndRemoteKeys(
            remoteKeys,
            electionList.toEntity(),
            loadType == LoadType.REFRESH,
            tpsId = tpsId
        )

        voteLocalDataSource.insertAll(
            electionList.toVoteFormEntities()
        )
        uploadEvidenceLocalDataSource.insertUploadedEvidence(electionList.toUploadedEvidenceEntities())
        
        return endOfPaginationReached
    }

    override suspend fun getRemoteKeyById(data: ElectionEntity): RemoteKeys? {
        return localDataSource.getRemoteKeyById(data.id)
    }
}

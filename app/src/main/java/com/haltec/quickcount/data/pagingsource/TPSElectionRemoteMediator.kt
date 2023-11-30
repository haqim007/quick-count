package com.haltec.quickcount.data.pagingsource

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.haltec.quickcount.data.local.dataSource.TPSElectionLocalDataSource
import com.haltec.quickcount.data.local.entity.table.RemoteKeys
import com.haltec.quickcount.data.local.entity.table.ElectionEntity
import com.haltec.quickcount.data.local.entity.view.TPSElectionEntity
import com.haltec.quickcount.data.local.entity.view.TPS_ELECTION_VIEW
import com.haltec.quickcount.data.mechanism.CustomThrowable
import com.haltec.quickcount.data.mechanism.CustomThrowable.Companion.UNKNOWN_HOST_EXCEPTION
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.base.checkToken
import com.haltec.quickcount.data.remote.datasource.TPSElectionRemoteDataSource
import com.haltec.quickcount.data.remote.response.toElectionEntities
import com.haltec.quickcount.data.remote.response.toTPSEntities
import com.haltec.quickcount.data.remote.response.toUploadedEvidenceEntities
import com.haltec.quickcount.data.remote.response.toVoteFormEntities

@OptIn(ExperimentalPagingApi::class)
class TPSElectionRemoteMediator (
    private val userPreference: UserPreference,
    private val localDataSource: TPSElectionLocalDataSource,
    private val remoteDataSource: TPSElectionRemoteDataSource,
    private val filter: String = "",
    private val isOnline: suspend() -> Boolean = {true}
): RemoteMediator<Int, TPSElectionEntity>(){
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TPSElectionEntity>,
    ): MediatorResult {
        val page = when(loadType){
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }
        
        val result = try {
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
            
            MediatorResult.Success(endOfPaginationReached)
        }
        catch (e: CustomThrowable){
            if (e.code == UNKNOWN_HOST_EXCEPTION && !isOnline()){
                MediatorResult.Success(true)
            }else{
                MediatorResult.Error(e)
            }
        }
        catch (e: Exception){
            MediatorResult.Error(e)
        }
        
        return result
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, TPSElectionEntity>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.let { data ->
                localDataSource.getRemoteKeyById(data.electionId)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, TPSElectionEntity>): RemoteKeys?{
        return state.pages
            .firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { data ->
                localDataSource.getRemoteKeyById(data.electionId)
            }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, TPSElectionEntity>): RemoteKeys?{
        return state.pages.lastOrNull{it.data.isNotEmpty()}?.data?.lastOrNull()?.let { data ->
            localDataSource.getRemoteKeyById(data.electionId)
        }
    }

    companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

}

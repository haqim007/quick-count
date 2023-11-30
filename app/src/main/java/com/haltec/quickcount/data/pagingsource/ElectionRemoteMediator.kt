package com.haltec.quickcount.data.pagingsource

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.haltec.quickcount.data.local.dataSource.ElectionLocalDataSource
import com.haltec.quickcount.data.local.entity.table.ELECTION_TABLE
import com.haltec.quickcount.data.local.entity.table.RemoteKeys
import com.haltec.quickcount.data.local.entity.table.ElectionEntity
import com.haltec.quickcount.data.mechanism.CustomThrowable
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.base.checkToken
import com.haltec.quickcount.data.remote.datasource.ElectionRemoteDataSource
import com.haltec.quickcount.data.remote.response.toEntity
import com.haltec.quickcount.data.remote.response.toUploadedEvidenceEntities
import com.haltec.quickcount.data.remote.response.toVoteFormEntities

@OptIn(ExperimentalPagingApi::class)
class ElectionRemoteMediator(
    private val userPreference: UserPreference,
    private val localDataSource: ElectionLocalDataSource,
    private val remoteDataSource: ElectionRemoteDataSource,
    private val tpsId: Int
): RemoteMediator<Int, ElectionEntity>(){
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ElectionEntity>,
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
            val response = checkToken(userPreference) { remoteDataSource.getElectionList(tpsId) }
            val endOfPaginationReached = true
            val prevKey = null //if(page == 1) null else page - 1
            val nextKey = null //if (endOfPaginationReached) null else page + 1
            val electionList = response.getOrThrow().data ?: listOf()
            val remoteKeys = electionList.map {
                RemoteKeys(
                    tableId = it.id,
                    tableName = ELECTION_TABLE,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }
            
            localDataSource.insertAllAndRemoteKeys(
                remoteKeys, 
                electionList.toEntity(tpsId), 
                electionList.toVoteFormEntities(),
                electionList.toUploadedEvidenceEntities(),
                loadType == LoadType.REFRESH
            )
            
            
            MediatorResult.Success(endOfPaginationReached)
        }
        catch (e: CustomThrowable){
            MediatorResult.Error(e)
        }
        catch (e: Exception){
            MediatorResult.Error(e)
        }
        
        return result
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, ElectionEntity>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                localDataSource.getRemoteKeyById(id)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ElectionEntity>): RemoteKeys?{
        return state.pages
            .firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { data ->
                localDataSource.getRemoteKeyById(data.id)
            }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ElectionEntity>): RemoteKeys?{
        return state.pages.lastOrNull{it.data.isNotEmpty()}?.data?.lastOrNull()?.let { data ->
            localDataSource.getRemoteKeyById(data.id)
        }
    }

    companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

}

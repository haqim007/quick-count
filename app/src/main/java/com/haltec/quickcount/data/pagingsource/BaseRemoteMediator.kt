package com.haltec.quickcount.data.pagingsource

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.haltec.quickcount.data.local.dataSource.TPSElectionLocalDataSource
import com.haltec.quickcount.data.local.entity.table.RemoteKeys
import com.haltec.quickcount.data.local.entity.view.TPS_ELECTION_VIEW
import com.haltec.quickcount.data.mechanism.CustomThrowable
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.base.checkToken
import com.haltec.quickcount.data.remote.datasource.TPSElectionRemoteDataSource
import com.haltec.quickcount.data.remote.response.toElectionEntities
import com.haltec.quickcount.data.remote.response.toTPSEntities
import com.haltec.quickcount.data.remote.response.toUploadedEvidenceEntities
import com.haltec.quickcount.data.remote.response.toVoteFormEntities

@OptIn(ExperimentalPagingApi::class)
abstract class BaseRemoteMediator<EntityType: Any> (
    private val userPreference: UserPreference,
    private val isOnline: suspend() -> Boolean = {true}
): RemoteMediator<Int, EntityType>(){
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EntityType>,
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
            val endOfPaginationReached = processData(page, loadType)
            MediatorResult.Success(endOfPaginationReached)
        }
        catch (e: CustomThrowable){
            if (
                (e.code == CustomThrowable.UNKNOWN_HOST_EXCEPTION && !isOnline()) ||
                e.code == CustomThrowable.NOT_FOUND
            ){
                MediatorResult.Success(true)
            }
            else{
                MediatorResult.Error(e)
            }
        }
        catch (e: Exception){
            MediatorResult.Error(e)
        }

        return result
    }


    /**
     * Process data
     *
     * @param page
     * @param loadType
     * @return endOfPaginationReached
     */
    protected open suspend fun processData(page: Int, loadType: LoadType): Boolean = false

    abstract suspend fun getRemoteKeyById(data: EntityType): RemoteKeys?

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, EntityType>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.let { data ->
                getRemoteKeyById(data)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, EntityType>): RemoteKeys?{
        return state.pages
            .firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { data ->
                getRemoteKeyById(data)
            }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, EntityType>): RemoteKeys?{
        return state.pages.lastOrNull{it.data.isNotEmpty()}?.data?.lastOrNull()?.let { data ->
            getRemoteKeyById(data)
        }
    }

    companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

}
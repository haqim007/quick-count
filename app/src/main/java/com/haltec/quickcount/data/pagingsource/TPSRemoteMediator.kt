package com.haltec.quickcount.data.pagingsource

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.haltec.quickcount.data.local.dataSource.TPSLocalDataSource
import com.haltec.quickcount.data.local.entity.table.RemoteKeys
import com.haltec.quickcount.data.local.entity.table.TPSEntity
import com.haltec.quickcount.data.local.entity.table.TPS_TABLE
import com.haltec.quickcount.data.local.entity.table.toEntity
import com.haltec.quickcount.data.mechanism.CustomThrowable
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.base.checkToken
import com.haltec.quickcount.data.remote.datasource.TPSRemoteDataSource
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
@ViewModelScoped
class TPSRemoteMediator @Inject constructor(
    private val userPreference: UserPreference,
    private val tpsLocalDataSource: TPSLocalDataSource,
    private val tpsRemoteDataSource: TPSRemoteDataSource
): RemoteMediator<Int, TPSEntity>(){
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, TPSEntity>,
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
            val response = checkToken(userPreference) { tpsRemoteDataSource.getTPSList() }
            val endOfPaginationReached = true
            val prevKey = null //if(page == 1) null else page - 1
            val nextKey = null //if (endOfPaginationReached) null else page + 1
            val tpsList = response.getOrThrow().data ?: listOf()
            val remoteKeys = tpsList.map {
                RemoteKeys(
                    tableId = it.id,
                    tableName = TPS_TABLE,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }
            
            tpsLocalDataSource.insertAllAndRemoteKeys(
                remoteKeys, 
                tpsList.toEntity(), 
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

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, TPSEntity>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                tpsLocalDataSource.getRemoteKeyById(id)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, TPSEntity>): RemoteKeys?{
        return state.pages
            .firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { data ->
                tpsLocalDataSource.getRemoteKeyById(data.id)
            }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, TPSEntity>): RemoteKeys?{
        return state.pages.lastOrNull{it.data.isNotEmpty()}?.data?.lastOrNull()?.let { data ->
            tpsLocalDataSource.getRemoteKeyById(data.id)
        }
    }

    companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

}

package com.haltec.quickcount.data.pagingsource

import androidx.paging.LoadType
import com.haltec.quickcount.data.local.dataSource.TPSLocalDataSource
import com.haltec.quickcount.data.local.entity.table.RemoteKeys
import com.haltec.quickcount.data.local.entity.table.TPSEntity
import com.haltec.quickcount.data.local.entity.table.TPS_TABLE
import com.haltec.quickcount.data.local.entity.table.toEntity
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.base.checkToken
import com.haltec.quickcount.data.remote.datasource.TPSRemoteDataSource


class TPSRemoteMediator (
    private val userPreference: UserPreference,
    private val tpsLocalDataSource: TPSLocalDataSource,
    private val tpsRemoteDataSource: TPSRemoteDataSource,
    isOnline: suspend () -> Boolean
): BaseRemoteMediator<TPSEntity>(userPreference, isOnline){

    override suspend fun processData(page: Int, loadType: LoadType): Boolean {
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
        
        return endOfPaginationReached
    }

    override suspend fun getRemoteKeyById(data: TPSEntity): RemoteKeys? {
        return tpsLocalDataSource.getRemoteKeyById(data.id)
    }

}

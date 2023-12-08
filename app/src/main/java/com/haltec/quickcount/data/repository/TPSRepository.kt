package com.haltec.quickcount.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.haltec.quickcount.data.local.dataSource.TPSLocalDataSource
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.pagingsource.TPSRemoteMediator
import com.haltec.quickcount.data.preference.DevicePreference
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.datasource.TPSRemoteDataSource
import com.haltec.quickcount.util.DEFAULT_PAGE_SIZE
import com.haltec.quickcount.di.DispatcherIO
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.repository.ITPSRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TPSRepository @Inject constructor(
    private val remoteDataSource: TPSRemoteDataSource,
    private val userPreference: UserPreference,
    @DispatcherIO
    private val dispatcher: CoroutineDispatcher,
    private val localDataSource: TPSLocalDataSource,
    private val devicePreference: DevicePreference
): ITPSRepository {

    override suspend fun getUsername(): String {
        return userPreference.getUserName().first()
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getTPSList(): Flow<PagingData<TPS>> {
        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE
            ),
            remoteMediator = TPSRemoteMediator(
                userPreference = userPreference,
                tpsLocalDataSource = localDataSource,
                tpsRemoteDataSource = remoteDataSource
            ){
                devicePreference.isOnline().first()
            },
            pagingSourceFactory = {
                localDataSource.getPaging()
            }
        ).flow.map { 
            it.map { 
                it.toModel()
            }
        }.flowOn(dispatcher)
    }

    override fun getTPS(tpsId: Int): Flow<Resource<TPS>> {
        //TODO("Not yet implemented")
        return flowOf()
    }
}
package com.haltec.quickcount.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.haltec.quickcount.data.local.dataSource.TPSElectionLocalDataSource
import com.haltec.quickcount.data.pagingsource.TPSElectionRemoteMediator
import com.haltec.quickcount.data.preference.DevicePreference
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.datasource.TPSElectionRemoteDataSource
import com.haltec.quickcount.util.DEFAULT_PAGE_SIZE
import com.haltec.quickcount.di.DispatcherIO
import com.haltec.quickcount.domain.model.ElectionFilter
import com.haltec.quickcount.domain.model.TPSElection
import com.haltec.quickcount.domain.model.submitVoteStatus
import com.haltec.quickcount.domain.repository.ITPSElectionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TPSElectionRepository @Inject constructor(
    private val remoteDataSource: TPSElectionRemoteDataSource,
    private val userPreference: UserPreference,
    private val devicePreference: DevicePreference,
    @DispatcherIO
    private val dispatcher: CoroutineDispatcher,
    private val localDataSource: TPSElectionLocalDataSource
): ITPSElectionRepository {
    
    @OptIn(ExperimentalPagingApi::class)
    override fun getTPSElections(filter: ElectionFilter) : Flow<PagingData<TPSElection>> {
        val submitVoteFilter = filter.submitVoteStatus
        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE
            ),
            remoteMediator = TPSElectionRemoteMediator(
                userPreference,
                localDataSource,
                remoteDataSource,
                submitVoteFilter
            ) {
                devicePreference.isOnline().first()
            },
            pagingSourceFactory = {
                submitVoteFilter?.valueNumber?.let {
                    localDataSource.getPaging(it)
                }?: run {
                    localDataSource.getPaging()
                }
                
            }
        ).flow.map {
            it.map {
                it.toModel()
            }
        }.flowOn(dispatcher)
    }
}
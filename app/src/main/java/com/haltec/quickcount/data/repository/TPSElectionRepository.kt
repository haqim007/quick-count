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
import com.haltec.quickcount.domain.model.TPSElection
import com.haltec.quickcount.domain.model.stringToSubmitVoteStatusNumber
import com.haltec.quickcount.domain.model.stringToSubmitVoteStatusValueText
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
    override fun getTPSElections(filter: String) : Flow<PagingData<TPSElection>> {
        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE
            ),
            remoteMediator = TPSElectionRemoteMediator(
                userPreference,
                localDataSource,
                remoteDataSource,
                stringToSubmitVoteStatusValueText(filter)
            ) {
                devicePreference.isOnline().first() ?: false
            },
            pagingSourceFactory = {
                localDataSource.getPaging(stringToSubmitVoteStatusNumber(filter))
            }
        ).flow.map {
            it.map {
                it.toModel()
            }
        }.flowOn(dispatcher)
    }
}
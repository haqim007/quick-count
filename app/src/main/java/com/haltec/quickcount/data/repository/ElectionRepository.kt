package com.haltec.quickcount.data.repository

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.haltec.quickcount.data.local.dataSource.ElectionLocalDataSource
import com.haltec.quickcount.data.local.dataSource.UploadEvidenceLocalDataSource
import com.haltec.quickcount.data.local.dataSource.VoteLocalDataSource
import com.haltec.quickcount.data.pagingsource.ElectionRemoteMediator
import com.haltec.quickcount.data.preference.DevicePreference
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.datasource.ElectionRemoteDataSource
import com.haltec.quickcount.util.DEFAULT_PAGE_SIZE
import com.haltec.quickcount.di.DispatcherIO
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.repository.IElectionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.lang.ref.WeakReference
import javax.inject.Inject


class ElectionRepository @Inject constructor(
    private val remoteDataSource: ElectionRemoteDataSource,
    private val userPreference: UserPreference,
    @DispatcherIO
    private val dispatcher: CoroutineDispatcher,
    private val localDataSource: ElectionLocalDataSource,
    private val devicePreference: DevicePreference,
    private val voteLocalDataSource: VoteLocalDataSource,
    private val uploadEvidenceLocalDataSource: UploadEvidenceLocalDataSource
): IElectionRepository {
    @OptIn(ExperimentalPagingApi::class)
    override fun getElectionList(tpsId: Int): Flow<PagingData<Election>> {
        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE
            ),
            remoteMediator = ElectionRemoteMediator(
                userPreference, 
                localDataSource, 
                remoteDataSource, 
                voteLocalDataSource,
                uploadEvidenceLocalDataSource,
                tpsId
            ){
                devicePreference.isOnline().first()
            },
            pagingSourceFactory = {
                localDataSource.getPaging(tpsId)
            }
        ).flow.map {
            it.map {
                it.toModel()
            }
        }.flowOn(dispatcher)
    }
}
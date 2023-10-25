package com.haltec.quickcount.data.repository

import android.util.Log
import com.haltec.quickcount.data.mechanism.AuthorizedNetworkBoundResource
import com.haltec.quickcount.data.mechanism.CustomThrowable
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.datasource.ElectionRemoteDataSource
import com.haltec.quickcount.data.remote.response.ElectionListResponse
import com.haltec.quickcount.di.DispatcherIO
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.repository.IElectionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


class ElectionRepository @Inject constructor(
    private val remoteDataSource: ElectionRemoteDataSource,
    private val userPreference: UserPreference,
    @DispatcherIO
    private val dispatcher: CoroutineDispatcher
): IElectionRepository {
    override fun getElectionList(tpsId: Int): Flow<Resource<List<Election>>> {
        return object: AuthorizedNetworkBoundResource<List<Election>, ElectionListResponse>(userPreference){

            override suspend fun requestFromRemote(): Result<ElectionListResponse> {
                return remoteDataSource.getElectionList(tpsId)
            }

            override fun loadResult(data: ElectionListResponse): Flow<List<Election>> {
                return flowOf(data.toModel())
            }
        }.asFlow().flowOn(dispatcher)
    }
}
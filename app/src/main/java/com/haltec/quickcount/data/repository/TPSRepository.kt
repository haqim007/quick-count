package com.haltec.quickcount.data.repository

import com.haltec.quickcount.data.mechanism.AuthorizedNetworkBoundResource
import com.haltec.quickcount.data.mechanism.CustomThrowable
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.datasource.TPSRemoteDataSource
import com.haltec.quickcount.data.remote.response.TPSElectionListResponse
import com.haltec.quickcount.data.remote.response.TPSListResponse
import com.haltec.quickcount.di.DispatcherIO
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.model.TPSElection
import com.haltec.quickcount.domain.model.stringToFilter
import com.haltec.quickcount.domain.repository.ITPSRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class TPSRepository @Inject constructor(
    private val remoteDataSource: TPSRemoteDataSource,
    private val userPreference: UserPreference,
    @DispatcherIO
    private val dispatcher: CoroutineDispatcher
): ITPSRepository {

    override suspend fun getUsername(): String {
        return userPreference.getUserName().first()
    }

    override fun getTPSList(): Flow<Resource<List<TPS>>> {
        return object: AuthorizedNetworkBoundResource<List<TPS>, TPSListResponse>(userPreference){

            override suspend fun requestFromRemote(): Result<TPSListResponse> {
                return remoteDataSource.getTPSList()
            }

            override fun loadResult(data: TPSListResponse): Flow<List<TPS>> {
                return flowOf(data.data?.map { it.toModel() } ?: listOf())
            }
            
        }.asFlow().flowOn(dispatcher)
    }

    override fun getTPSElections(filter: String) : Flow<Resource<List<TPSElection>>> {
        return object: AuthorizedNetworkBoundResource<List<TPSElection>, TPSElectionListResponse>(userPreference){

            override suspend fun requestFromRemote(): Result<TPSElectionListResponse> {
                val res = remoteDataSource.getTPSElectionList(stringToFilter(filter))
                val exception = res.exceptionOrNull() as? CustomThrowable
                return exception?.let { 
                    if (it.code == 404){
                        Result.success(
                            TPSElectionListResponse(
                                data = listOf(), 
                                it.data?.message ?: it.localizedMessage, 
                                it.data?.version ?: ""
                            )
                        )
                    }else {
                        res
                    }
                } ?: res
            }

            override fun loadResult(data: TPSElectionListResponse): Flow<List<TPSElection>> {
                return flowOf(data.data?.map { it.toModel() } ?: listOf())
            }

        }.asFlow().flowOn(dispatcher)
    }

    override fun getTPS(tpsId: Int): Flow<Resource<TPS>> {
        //TODO("Not yet implemented")
        return flowOf()
    }
}
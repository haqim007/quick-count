package com.haltec.quickcount.data.repository

import android.content.Context
import com.haltec.quickcount.data.local.dataSource.TPSElectionLocalDataSource
import com.haltec.quickcount.data.mechanism.AuthorizedNetworkBoundResource
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.datasource.OfflineRemoteDataSource
import com.haltec.quickcount.data.remote.response.OfflineResponse
import com.haltec.quickcount.data.remote.response.toEntity
import com.haltec.quickcount.data.remote.response.toUploadedEvidenceEntities
import com.haltec.quickcount.data.remote.response.toVoteFormEntities
import com.haltec.quickcount.di.DispatcherIO
import com.haltec.quickcount.domain.model.BasicMessage
import com.haltec.quickcount.domain.repository.IOfflineRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import java.lang.ref.WeakReference
import javax.inject.Inject

class OfflineRepository @Inject constructor(
    private val remoteDataSource: OfflineRemoteDataSource,
    private val userPreference: UserPreference,
    private val localDataSource: TPSElectionLocalDataSource,
    @DispatcherIO
    private val dispatcher: CoroutineDispatcher,
    @ApplicationContext context: Context
): IOfflineRepository {
    private val weakContext = WeakReference(context)
    
    override fun getAllData(): Flow<Resource<BasicMessage>> {
        return object: AuthorizedNetworkBoundResource<BasicMessage, OfflineResponse>(userPreference){
            override suspend fun requestFromRemote(): Result<OfflineResponse> {
                return remoteDataSource.getAllData()
            }

            override suspend fun onSuccess(data: OfflineResponse) {
                val tpsEntities = data.data.map { it.toEntity() }
                val electionEntities = data.data.flatMap { 
                    it.selectionTypeList.toEntity(it.id)
                }
                val voteFormEntities = data.data.flatMap { 
                    it.selectionTypeList.toVoteFormEntities()
                }.filter { 
                    it.tpsId != 0
                }
                val uploadedEvidence = data.data.flatMap {
                    weakContext.get()
                        ?.let { nonNullContext ->
                            it.selectionTypeList.toUploadedEvidenceEntities(nonNullContext)
                        } ?: emptyList()
                }
                localDataSource.insertAll(
                    tpsEntities,
                    electionEntities,
                    voteFormEntities,
                    uploadedEvidence
                )
            }

            override fun loadResult(data: OfflineResponse): Flow<BasicMessage> {
                return flowOf(BasicMessage("succeed!"))
            }
        }.asFlow().flowOn(dispatcher)
    }
}
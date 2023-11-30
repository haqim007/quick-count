package com.haltec.quickcount.data.repository

import android.content.Context
import com.haltec.quickcount.data.local.dataSource.UploadEvidenceLocalDataSource
import com.haltec.quickcount.data.local.entity.table.TempUploadEvidenceEntity
import com.haltec.quickcount.data.local.entity.table.UploadedEvidenceEntity
import com.haltec.quickcount.data.local.entity.table.toModel
import com.haltec.quickcount.data.mechanism.AuthorizedNetworkBoundResource
import com.haltec.quickcount.data.mechanism.CustomThrowable
import com.haltec.quickcount.data.mechanism.CustomThrowable.Companion.UNKNOWN_HOST_EXCEPTION
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.datasource.UploadEvidenceRemoteDataSource
import com.haltec.quickcount.data.remote.request.UploadEvidenceRequest
import com.haltec.quickcount.data.remote.response.CurrentEvidenceResponse
import com.haltec.quickcount.data.remote.response.UploadEvidenceResponse
import com.haltec.quickcount.di.DispatcherIO
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.model.VoteEvidence
import com.haltec.quickcount.domain.model.stringToEvidenceType
import com.haltec.quickcount.domain.repository.IUploadEvidenceRepository
import com.haltec.quickcount.util.currentTimestamp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.ref.WeakReference
import javax.inject.Inject

class UploadEvidenceRepository @Inject constructor(
    private val userPreference: UserPreference,
    private val remoteDataSource: UploadEvidenceRemoteDataSource,
    private val localDataSource: UploadEvidenceLocalDataSource,
    @DispatcherIO
    private val dispatcher: CoroutineDispatcher,
    @ApplicationContext context: Context
): IUploadEvidenceRepository {
    private val weakContext = WeakReference(context)
    override fun upload(
        tps: TPS,
        election: Election,
        latitude: Double,
        longitude: Double,
        type: String,
        description: String,
        image: File,
    ): Flow<Resource<VoteEvidence>> {
        return object: AuthorizedNetworkBoundResource<VoteEvidence, UploadEvidenceResponse>(
            userPreference
        ){
            override suspend fun requestFromRemote(): Result<UploadEvidenceResponse> {
                return remoteDataSource.upload(
                    tpsId = tps.id,
                    electionId = election.id,
                    type = stringToEvidenceType(type).value,
                    description = description,
                    latitude = latitude.toString(),
                    longitude = longitude.toString(),
                    file = image
                )
            }

            override suspend fun onFailed(exceptionOrNull: CustomThrowable?) {
                if (exceptionOrNull?.code == UNKNOWN_HOST_EXCEPTION){
                    localDataSource.insertOrReplaceUploadEvidence(
                        UploadedEvidenceEntity(
                            tpsId = tps.id,
                            electionId = election.id,
                            longitude = longitude.toString(),
                            latitude = latitude.toString(),
                            description = description,
                            type = type,
                            file = image,
                            fileUrl = null,
                            uploadedAt = "", // for placeholder only. it will not be used
                            id = currentTimestamp().toInt()
                        ),
                        TempUploadEvidenceEntity(
                            tpsId = tps.id,
                            electionId = election.id,
                            longitude = longitude.toString(),
                            latitude = latitude.toString(),
                            description = description,
                            type = type,
                            file = image
                        )
                    )
                    
                }
            }

            override fun loadResult(data: UploadEvidenceResponse): Flow<VoteEvidence> {
                return flowOf(data.toModel())
            }
        }.asFlow()
    }

    override fun upload(
        request: UploadEvidenceRequest
    ): Flow<Resource<VoteEvidence>> {
        return object: AuthorizedNetworkBoundResource<VoteEvidence, UploadEvidenceResponse>(
            userPreference
        ){
            override suspend fun requestFromRemote(): Result<UploadEvidenceResponse> {
                return remoteDataSource.upload(
                    tpsId = request.tpsId,
                    electionId = request.electionId,
                    type = stringToEvidenceType(request.type).value,
                    description = request.description,
                    latitude = request.latitude,
                    longitude = request.longitude,
                    file = request.image
                )
            }

            override suspend fun onSuccess(data: UploadEvidenceResponse) {
                super.onSuccess(data)
                localDataSource.removeTempUploadEvidence(request.tpsId, request.electionId, request.type)
//                request.image.delete() //if after submit, navigate back directly. use this to delete the uploaded image
            }

            override fun loadResult(data: UploadEvidenceResponse): Flow<VoteEvidence> {
                return flowOf(data.toModel())
            }
        }.asFlow()
    }

    override fun getPrevData(
        tps: TPS,
        election: Election,
    ): Flow<Resource<List<VoteEvidence>>> {
        return object: AuthorizedNetworkBoundResource<List<VoteEvidence>, CurrentEvidenceResponse>(
            userPreference
        ){

            override suspend fun loadCurrentLocalData(): List<VoteEvidence> {
                return localDataSource.getUploadedEvidence(tps.id, election.id).toModel()
            }
            
            override suspend fun requestFromRemote(): Result<CurrentEvidenceResponse> {
                return remoteDataSource.getData(
                    tpsId = tps.id,
                    electionId = election.id
                )
            }

            override suspend fun onSuccess(data: CurrentEvidenceResponse) {
                weakContext.get()?.let {
                    localDataSource.insertUploadedEvidence(data.toUploadedEntities(it))
                }?: run{
                    localDataSource.insertUploadedEvidence(data.toUploadedEntities())
                }
            }

            override fun loadResult(data: CurrentEvidenceResponse): Flow<List<VoteEvidence>> {
                return flowOf(data.toModel())
            }
        }.asFlow()
    }

    override suspend fun getTempUploadEvidence(): List<UploadEvidenceRequest> {
        return withContext(dispatcher){
            localDataSource.getTempUploadEvidence().map { 
                
                UploadEvidenceRequest(
                    tpsId = it.tpsId,
                    electionId = it.electionId,
                    type = it.type,
                    description = it.description,
                    longitude = it.longitude,
                    latitude = it.latitude,
                    image = it.file
                )
                
            }
        }
    }
}
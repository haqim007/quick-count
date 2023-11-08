package com.haltec.quickcount.data.repository

import com.haltec.quickcount.data.mechanism.AuthorizedNetworkBoundResource
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.datasource.UploadEvidenceRemoteDataSource
import com.haltec.quickcount.data.remote.response.CurrentEvidenceResponse
import com.haltec.quickcount.data.remote.response.UploadEvidenceResponse
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.model.VoteEvidence
import com.haltec.quickcount.domain.repository.IUploadEvidenceRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.io.File
import javax.inject.Inject

@ViewModelScoped
class UploadEvidenceRepository @Inject constructor(
    private val userPreference: UserPreference,
    private val remoteDataSource: UploadEvidenceRemoteDataSource
): IUploadEvidenceRepository {
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
                    type = type,
                    description = description,
                    latitude = tps.latitude,
                    longitude = tps.longitude,
                    file = image
                )
            }

            override fun loadResult(data: UploadEvidenceResponse): Flow<VoteEvidence> {
                return flowOf(data.toModel())
            }
        }.asFlow()
    }

    override fun getCurrent(
        tps: TPS,
        election: Election,
    ): Flow<Resource<List<VoteEvidence>>> {
        return object: AuthorizedNetworkBoundResource<List<VoteEvidence>, CurrentEvidenceResponse>(
            userPreference
        ){
            override suspend fun requestFromRemote(): Result<CurrentEvidenceResponse> {
                return remoteDataSource.get(
                    tpsId = tps.id,
                    electionId = election.id
                )
            }

            override fun loadResult(data: CurrentEvidenceResponse): Flow<List<VoteEvidence>> {
                return flowOf(data.toModel())
            }
        }.asFlow()
    }
}
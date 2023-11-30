package com.haltec.quickcount.domain.repository

import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.remote.request.UploadEvidenceRequest
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.model.VoteEvidence
import kotlinx.coroutines.flow.Flow
import java.io.File

interface IUploadEvidenceRepository {
    fun upload(
        tps: TPS, election: Election, 
        latitude: Double, longitude: Double,
        type: String, description: String, 
        image: File
    ): Flow<Resource<VoteEvidence>>

    fun getPrevData(tps: TPS, election: Election): Flow<Resource<List<VoteEvidence>>>
    suspend fun getTempUploadEvidence(): List<UploadEvidenceRequest>
    fun upload(request: UploadEvidenceRequest): Flow<Resource<VoteEvidence>>
}
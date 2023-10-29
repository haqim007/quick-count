package com.haltec.quickcount.domain.repository

import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.model.UploadEvidenceResult
import kotlinx.coroutines.flow.Flow
import java.io.File

interface IUploadEvidenceRepository {
    fun upload(
        tps: TPS, election: Election, 
        latitude: Double, longitude: Double,
        type: String, description: String, 
        image: File
    ): Flow<Resource<UploadEvidenceResult>>
    
}
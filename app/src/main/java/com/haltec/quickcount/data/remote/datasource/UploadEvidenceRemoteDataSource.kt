package com.haltec.quickcount.data.remote.datasource

import com.haltec.quickcount.data.mechanism.getResult
import com.haltec.quickcount.data.remote.service.UploadEvidenceService
import com.haltec.quickcount.data.util.multipartRequestBody
import com.haltec.quickcount.data.util.textPlainRequestBody
import dagger.hilt.android.scopes.ViewModelScoped
import java.io.File
import javax.inject.Inject

@ViewModelScoped
class UploadEvidenceRemoteDataSource @Inject constructor(
    private val service: UploadEvidenceService
) {
    
    suspend fun get(tpsId: Int, electionId: Int) = getResult { 
        service.get(tpsId, electionId)
    }
    
    suspend fun upload(
        tpsId: Int,
        electionId: Int,
        type: String,
        description: String,
        longitude: String,
        latitude: String,
        file: File
    ) = getResult { 
        service.upload(
            tpsId,
            electionId,
            textPlainRequestBody(type),
            textPlainRequestBody(description),
            textPlainRequestBody(longitude),
            textPlainRequestBody(latitude),
            file = multipartRequestBody(file, name = "file")
        )
    }
}
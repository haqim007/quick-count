package com.haltec.quickcount.data.remote.datasource

import com.haltec.quickcount.data.mechanism.getResult
import com.haltec.quickcount.data.remote.service.UploadEvidenceService
import com.haltec.quickcount.data.util.fileRequestBody
import com.haltec.quickcount.data.util.multipartRequestBody
import dagger.hilt.android.scopes.ViewModelScoped
import java.io.File
import javax.inject.Inject

@ViewModelScoped
class UploadEvidenceRemoteDataSource @Inject constructor(
    private val service: UploadEvidenceService
) {
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
            type,
            description,
            longitude,
            latitude,
            file = multipartRequestBody(file, name = "file")
        )
    }
}
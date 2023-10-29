package com.haltec.quickcount.data.remote.service

import com.haltec.quickcount.data.remote.response.UploadEvidenceResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadEvidenceService {
    @Multipart
    @POST("attachment-file")
    suspend fun upload(
        @Part("TpsId")
        tpsId: Int,
        @Part("SelectionTypeId")
        electionId: Int,
        @Part("Type")
        type: String,
        @Part("Description")
        description: String,
        @Part("Longitude")
        longitude: String,
        @Part("Latitude")
        latitude: String,
        @Part
        file: MultipartBody.Part
    ): UploadEvidenceResponse
}
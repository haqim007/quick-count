package com.haltec.quickcount.data.remote.service

import com.haltec.quickcount.data.remote.response.CurrentEvidenceResponse
import com.haltec.quickcount.data.remote.response.UploadEvidenceResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface UploadEvidenceService {
    @Multipart
    @POST("attachment-file")
    suspend fun upload(
        @Part("TpsId")
        tpsId: Int,
        @Part("SelectionTypeId")
        electionId: Int,
        @Part("Type")
        type: RequestBody,
        @Part("Description")
        description: RequestBody,
        @Part("Longitude")
        longitude: RequestBody,
        @Part("Latitude")
        latitude: RequestBody,
        @Part
        file: MultipartBody.Part
    ): UploadEvidenceResponse
    
    @GET("attachment-file/{tpsId}/{electionId)")
    suspend fun get(
        @Path("tpsId")
        tpsId: Int,
        @Path("electionId")
        electionId: Int
    ): CurrentEvidenceResponse
}
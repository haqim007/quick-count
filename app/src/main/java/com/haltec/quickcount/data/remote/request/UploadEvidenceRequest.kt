package com.haltec.quickcount.data.remote.request

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Part
import java.io.File

data class UploadEvidenceRequest(
    val tpsId: Int,
    val electionId: Int,
    val latitude: String,
    val longitude: String,
    val type: String,
    val description: String,
    val image: File,
)

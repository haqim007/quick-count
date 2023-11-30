package com.haltec.quickcount.data.remote.service

import com.haltec.quickcount.data.remote.response.OfflineResponse
import retrofit2.http.GET

interface OfflineService {
    
    @GET("offline")
    suspend fun getAllData(): OfflineResponse
}
package com.haltec.quickcount.data.remote.service

import com.haltec.quickcount.data.remote.response.TPSElectionListResponse
import com.haltec.quickcount.data.remote.response.TPSListResponse
import com.haltec.quickcount.data.remote.response.TPSResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TPSService {
    @GET("tps")
    suspend fun getTPSList(): TPSListResponse
    
    @GET("tps/{id}")
    suspend fun getTPS(@Path("id") tpsId: Int): TPSResponse

    @GET("tps/formulir")
    suspend fun getTPSElectionList(
        @Query("filter") filter: String = ""
    ): TPSElectionListResponse
}
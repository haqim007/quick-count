package com.haltec.quickcount.data.remote.service

import com.haltec.quickcount.data.remote.response.ElectionListResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ElectionService {
    @GET("selection-type/{tpsId}")
    suspend fun getElectionList(
        @Path("tpsId") tpsId: Int
    ): ElectionListResponse
}
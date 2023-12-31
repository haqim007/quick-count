package com.haltec.quickcount.data.remote.service

import com.haltec.quickcount.data.remote.request.VoteRequest
import com.haltec.quickcount.data.remote.response.BasicResponse
import com.haltec.quickcount.data.remote.response.VoteFormResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface VoteService {
    
    @GET("vote/{tpsId}/{electionId}")
    suspend fun getCandidates(
        @Path("tpsId")
        tpsId: Int,
        @Path("electionId")
        electionId: Int
    ): VoteFormResponse

    @POST("vote")
    suspend fun vote(
        @Body voteRequest: VoteRequest
    ): BasicResponse
}
package com.haltec.quickcount.data.remote.datasource

import com.haltec.quickcount.data.mechanism.getResult
import com.haltec.quickcount.data.remote.request.VoteRequest
import com.haltec.quickcount.data.remote.service.VoteService
import javax.inject.Inject

class VoteRemoteDataSource @Inject constructor(
    private val service: VoteService
) {
    suspend fun getCandidates(tpsId: Int, electionId: Int) = getResult { 
        service.getCandidates(tpsId, electionId)
    }
    
    suspend fun vote(voteRequest: VoteRequest) = getResult { 
        service.vote(voteRequest)
    }
}
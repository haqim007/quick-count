package com.haltec.quickcount.domain.repository

import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.remote.request.VoteRequest
import com.haltec.quickcount.domain.model.BasicMessage
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.model.VoteData
import kotlinx.coroutines.flow.Flow

interface IVoteRepository {
    fun getCandidateList(tps: TPS, election: Election): Flow<Resource<VoteData>>
    fun vote(
        tps: TPS, election: Election, invalidVote: Int,
        candidates: List<Pair<Int, Int>>,
        parties: List<Pair<Int, Int>>?,
        isParty: Boolean
    ): Flow<Resource<BasicMessage>>

    fun vote(
        voteRequest: VoteRequest
    ): Flow<Resource<BasicMessage>>
    
    suspend fun getTempVoteData(): List<VoteRequest>
}
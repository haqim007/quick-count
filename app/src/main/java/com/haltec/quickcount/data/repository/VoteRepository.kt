package com.haltec.quickcount.data.repository

import com.haltec.quickcount.data.mechanism.AuthorizedNetworkBoundResource
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.datasource.VoteRemoteDataSource
import com.haltec.quickcount.data.remote.request.VoteRequest
import com.haltec.quickcount.data.remote.response.BasicResponse
import com.haltec.quickcount.data.remote.response.CandidateListResponse
import com.haltec.quickcount.di.DispatcherIO
import com.haltec.quickcount.domain.model.BasicMessage
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.model.VoteData
import com.haltec.quickcount.domain.repository.IVoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VoteRepository @Inject constructor(
    private val remoteDataSource: VoteRemoteDataSource,
    private val userPreference: UserPreference,
    @DispatcherIO
    private val dispatcher: CoroutineDispatcher
): IVoteRepository {
    override fun getCandidateList(tps: TPS, election: Election): Flow<Resource<VoteData>> {
        return object: AuthorizedNetworkBoundResource<VoteData, CandidateListResponse>(
            userPreference
        ){
            override suspend fun requestFromRemote(): Result<CandidateListResponse> {
                return remoteDataSource.getCandidates(tps.id, election.id)
            }

            override fun loadResult(data: CandidateListResponse): Flow<VoteData> {
                return flowOf(data.toModel(tps.city))
            }
        }.asFlow().map { 
            if (it is Resource.Success && it.data != null){
                val data = it.data.copy(
                    partyLists = it.data.partyLists.mapIndexed { index, partyListsItem ->
                        partyListsItem.copy(
                            isExpanded = index == 0
                        )
                    }
                )
                Resource.Success(data)
            }else{
                it
            }
        }.flowOn(dispatcher)
    }

    override fun vote(
        tps: TPS,
        election: Election,
        invalidVote: Int,
        candidates: List<Pair<Int, Int>>,
        parties: List<Pair<Int, Int>>
    ): Flow<Resource<BasicMessage>> {
        return object : AuthorizedNetworkBoundResource<BasicMessage, BasicResponse>(
            userPreference
        ) {
            override suspend fun requestFromRemote(): Result<BasicResponse> {
                val totalCandidatesVotes = candidates.map { 
                    it.second
                }.sum()
                val totalPartiesVotes = parties.map { it.second }.sum()
                val validVotes = totalCandidatesVotes + totalPartiesVotes
                return remoteDataSource.vote(
                    VoteRequest(
                        tpsId = tps.id,
                        selectionTypeId = election.id,
                        validVote = validVotes,
                        invalidVote = invalidVote,
                        amount = validVotes + invalidVote,
                        candidate = candidates.map { 
                            VoteRequest.CandidateItem(it.first, it.second)
                        },
                        partai = parties.map {
                            VoteRequest.PartyItem(it.first, it.second)
                        }
                    )
                )
            }

            override fun loadResult(data: BasicResponse): Flow<BasicMessage> {
                return flowOf(BasicMessage(data.message))
            }
        }.asFlow()
    }
}
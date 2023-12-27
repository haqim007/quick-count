package com.haltec.quickcount.data.repository

import com.haltec.quickcount.data.local.dataSource.TPSLocalDataSource
import com.haltec.quickcount.data.local.dataSource.VoteLocalDataSource
import com.haltec.quickcount.data.local.entity.table.TempVoteCandidateEntity
import com.haltec.quickcount.data.local.entity.table.TempVotePartyEntity
import com.haltec.quickcount.data.local.entity.table.TempVoteSubmitEntity
import com.haltec.quickcount.data.mechanism.AuthorizedNetworkBoundResource
import com.haltec.quickcount.data.mechanism.CustomThrowable
import com.haltec.quickcount.data.mechanism.CustomThrowable.Companion.UNKNOWN_HOST_EXCEPTION
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.preference.DevicePreference
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.datasource.VoteRemoteDataSource
import com.haltec.quickcount.data.remote.request.VoteRequest
import com.haltec.quickcount.data.remote.response.BasicResponse
import com.haltec.quickcount.data.remote.response.VoteFormResponse
import com.haltec.quickcount.di.DispatcherIO
import com.haltec.quickcount.domain.model.BasicMessage
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.model.VoteData
import com.haltec.quickcount.domain.repository.IVoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VoteRepository @Inject constructor(
    private val remoteDataSource: VoteRemoteDataSource,
    private val userPreference: UserPreference,
    @DispatcherIO
    private val dispatcher: CoroutineDispatcher,
    private val localDataSource: VoteLocalDataSource,
    private val devicePreference: DevicePreference,
    private val tpsLocalDataSource: TPSLocalDataSource
): IVoteRepository {
    override fun getCandidateList(tps: TPS, election: Election): Flow<Resource<VoteData>> {
        return object: AuthorizedNetworkBoundResource<VoteData, VoteFormResponse>( userPreference ){
            override suspend fun requestFromRemote(): Result<VoteFormResponse> {
                return remoteDataSource.getCandidates(tps.id, election.id)
            }

            override suspend fun loadCurrentLocalData(): VoteData? {
                return localDataSource.getVoteForm(tps.id, election.id)?.toModel(tps.city)
            }

            // TODO: Should load from local instead
            override fun loadResult(data: VoteFormResponse): Flow<VoteData> {
                return flowOf(data.toModel(tps.city))
            }

            override suspend fun onSuccess(data: VoteFormResponse) {
                if (data.data.tpsId != 0){
                    localDataSource.insertOrReplace(data.toEntity())
                }
            }
            
        }.asFlow().map { 
            if (it is Resource.Success && it.data != null){
                val data = it.data.copy(
                    partyLists = it.data.partyLists
                )
                Resource.Success(data)
            }else if (it is Resource.Error && it.data == null && devicePreference.isOnline().first() == false){
                Resource.Error(
                    message = "Device offline",
                    code = it.code
                )
            }
            else{
                it
            }
        }.flowOn(dispatcher)
    }

    override fun vote(
        tps: TPS,
        election: Election,
        invalidVote: Int,
        candidates: List<Pair<Int, Int>>,
        parties: List<Pair<Int, Int>>?,
        isParty: Boolean,
        longitude: Double,
        latitude: Double
    ): Flow<Resource<BasicMessage>> {
        return object : AuthorizedNetworkBoundResource<BasicMessage, BasicResponse>(
            userPreference
        ) {
            override suspend fun requestFromRemote(): Result<BasicResponse> {
                val totalCandidatesVotes = candidates.sumOf {
                    it.second
                }
                val totalPartiesVotes = parties?.sumOf { it.second } ?: 0
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
                        partai = parties?.map {
                            VoteRequest.PartyItem(partaiId = it.first, amount = it.second)
                        },
                        isPartai = if (isParty) 1 else 0,
                        longitude = longitude.toString(),
                        latitude = latitude.toString()
                    )
                )
            }

            override fun loadResult(data: BasicResponse): Flow<BasicMessage> {
                return flowOf(BasicMessage(data.message))
            }

            override suspend fun onSuccess(data: BasicResponse) {
                localDataSource.onVoteSubmitSuccess(tpsId = tps.id, electionId = election.id)
            }

            override suspend fun onFailed(exceptionOrNull: CustomThrowable?) {
                if (exceptionOrNull?.code == UNKNOWN_HOST_EXCEPTION){
                    val totalCandidatesVotes = candidates.sumOf {
                        it.second
                    }
                    val totalPartiesVotes = parties?.sumOf { it.second } ?: 0
                    val validVotes = totalCandidatesVotes + totalPartiesVotes
                    localDataSource.insertOrReplaceTempVoteData(
                        tempVoteData = TempVoteSubmitEntity(
                            tpsId = tps.id,
                            electionId = election.id,
                            validVote = validVotes,
                            invalidVote = invalidVote,
                            amount = validVotes + invalidVote,
                            candidate = candidates.map {
                                TempVoteCandidateEntity(it.first, it.second)
                            },
                            partai = parties?.map {
                                TempVotePartyEntity(partaiId = it.first, amount = it.second)
                            },
                            isPartai = if (isParty) 1 else 0
                        )
                    )
                }
            }
        }.asFlow()
    }

    override fun vote(
        voteRequest: VoteRequest
    ): Flow<Resource<BasicMessage>> {
        return object : AuthorizedNetworkBoundResource<BasicMessage, BasicResponse>(
            userPreference
        ) {
            override suspend fun requestFromRemote(): Result<BasicResponse> {
                return remoteDataSource.vote(voteRequest)
            }

            override fun loadResult(data: BasicResponse): Flow<BasicMessage> {
                return flowOf(BasicMessage(data.message))
            }

            override suspend fun onSuccess(data: BasicResponse) {
                localDataSource.onVoteSubmitSuccess(voteRequest.tpsId, voteRequest.selectionTypeId)
            }
        }.asFlow()
    }

    override suspend fun getTempVoteData(longitude: Double, latitude: Double): List<VoteRequest> {
        return withContext(dispatcher){
            localDataSource.getTempVoteData().map { it.toRequest(longitude, latitude) }
        }
    }
}
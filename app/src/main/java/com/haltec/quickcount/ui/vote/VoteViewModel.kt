package com.haltec.quickcount.ui.vote

import androidx.lifecycle.viewModelScope
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.BasicMessage
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.model.VoteData
import com.haltec.quickcount.domain.repository.IVoteRepository
import com.haltec.quickcount.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoteViewModel @Inject constructor(
    private val repository: IVoteRepository
) : BaseViewModel<VoteUiState>() {
    override val _state = MutableStateFlow(VoteUiState())
    
    init {
        state.map { it.voteData }.distinctUntilChanged()
            .launchCollectLatest {
                if (!state.value.hasInitState && it is Resource.Success){
                    setInitState()
                }
            }
    }
    
    fun setTpsElection(tps: TPS, election: Election){
        _state.update { state ->
            state.copy(
                tps = tps,
                election = election
            )
        }
        fetchCandidates()
    }
    
    private fun setInitState(){
        var partiesVote = state.value.partiesVote
        var newCandidatesVote = state.value.candidatesVote
        state.value.voteData.data?.partyLists?.forEach { partyListsItem ->
            partiesVote = updatePartiesVoteState(partiesVote, partyListsItem.id, partyListsItem.totalPartyVote)

            newCandidatesVote = partyListsItem.candidateList.flatMap { candidateItem ->
                updateCandidatesVoteState(newCandidatesVote, candidateItem.id, candidateItem.totalCandidateVote)
           }
        }
   
        _state.update { state ->
            state.copy(
                invalidVote = state.voteData.data?.invalidVote ?: 0,
                partiesVote = partiesVote,
                candidatesVote = newCandidatesVote,
                hasInitState = true
            )
        }

    }

    private fun updateCandidatesVoteState(currentCandidatesVote:  List<Pair<Int, Int>>, candidateId: Int, vote: Int) =
        currentCandidatesVote.filter { pair ->
            pair.first != candidateId
        } + (candidateId to vote)

    private fun updatePartiesVoteState(currentPartiesVote: List<Pair<Int, Int>>, partyId: Int, vote: Int) =
        currentPartiesVote.filter { pair ->
            pair.first != partyId
        } + (partyId to vote)

    fun fetchCandidates(){
        if (state.value.tps != null && state.value.election != null){
            repository.getCandidateList(
                state.value.tps!!,
                state.value.election!!
            ).launchCollectLatest {
                _state.update { state -> 
                    state.copy(voteData = it) 
                }
            }
        }
    }
    
    private var updateCandidateVote: Job? = null
    fun setCandidateVote(partyId: Int, candidateId: Int, vote: Int){
        updateCandidateVote?.cancel()
        updateCandidateVote = viewModelScope.launch {
//            val newCandidatesVote = state.value.candidatesVote.filter{ pair ->
//                pair.first != candidateId
//            } + (candidateId to vote)
            val newCandidatesVote = updateCandidatesVoteState(state.value.candidatesVote, candidateId, vote)
            val newPartyListItem = updateCandidateVote(partyId, candidateId, vote)
            val newVoteData = state.value.voteData.data?.copy(
                partyLists = newPartyListItem,
            )
            _state.update { state ->
                state.copy(
                    hasInputData =  true,
                    candidatesVote = newCandidatesVote,
                    voteData = Resource.Success(newVoteData!!)
                )
            }

            delay(3000)
        }
    }

    private fun updateCandidateVote(
        partyId: Int,
        candidateId: Int,
        vote: Int,
    ): List<VoteData.PartyListsItem> {
        val newPartyListItem = state.value.voteData.data?.partyLists?.map {
            if (it.id == partyId) {
                it.copy(
                    totalVote = it.totalPartyVote + it.candidateList.sumOf { candidate ->
                        if (candidate.id == candidateId) {
                            vote
                        } else {
                            candidate.totalCandidateVote
                        }
                    },
                    candidateList = it.candidateList.map { candidate ->
                        if (candidate.id == candidateId) {
                            candidate.copy(
                                totalCandidateVote = vote
                            )
                        } else {
                            candidate
                        }
                    }
                )
            } else {
                it
            }
        } ?: listOf()
        return newPartyListItem
    }

    private var updatePartyVote: Job? = null
    fun setPartyVote(partyId: Int, vote: Int){
        updatePartyVote?.cancel()
        updatePartyVote = viewModelScope.launch {
//            val newPartiesVote = state.value.partiesVote.filter{ pair ->
//                pair.first != partyId
//            } + (partyId to vote)
            val newPartiesVote = updatePartiesVoteState(state.value.partiesVote, partyId, vote)
            val newPartyListItem = updatePartyVote(partyId, vote)
            val newVoteData = state.value.voteData.data?.copy(
                partyLists = newPartyListItem
            )
            _state.update { state ->
                state.copy(
                    hasInputData =  true,
                    partiesVote = newPartiesVote,
                    voteData = Resource.Success(newVoteData!!)
                )
            }
            delay(3000)
        }
    }
    
    fun getTotalVote(partyId: Int): Flow<Int> {
        return state
            .map { it.voteData.data?.partyLists?.first { it.id == partyId }?.totalVote ?: 0 }
            .distinctUntilChanged()
    }
    
    fun getCandidateListData(partyId: Int): Flow<List<VoteData.Candidate>> {
        return state.map { 
            val item = it.voteData.data?.partyLists?.first { it.id == partyId }
                ?.candidateList ?: listOf()
            //dummy
//            val result = mutableListOf<VoteData.Candidate>()
//            for (i in 99..105){
//                result.add(item[0].copy(
//                    id = i
//                ))
//            }
            //dummy
            item
        }
    }

    /**
     * Update party vote in [VoteData.PartyListsItem]
     *
     * @param partyId
     * @param vote
     * @return
     */
    private fun updatePartyVote(
        partyId: Int,
        vote: Int
    ): List<VoteData.PartyListsItem> {
        val newPartyListItem = state.value.voteData.data?.partyLists?.map {
            if (it.id == partyId) {
                it.copy(
                    totalPartyVote = vote,
                    totalVote = vote + it.candidateList.sumOf { candidate ->
                        candidate.totalCandidateVote
                    }
                )
            } else {
                it
            }
        } ?: listOf()
        return newPartyListItem
    }

    fun toggleView(partyId: Int){
        val newPartyListsItem = state.value.voteData.data?.partyLists?.map { party ->
            party.copy(
                isExpanded = if(party.id == partyId) !party.isExpanded else party.isExpanded
            )
        } ?: listOf()
        val newData = state.value.voteData.data!!.copy(
            partyLists = newPartyListsItem
        )
        _state.update { state ->
            state.copy(
                voteData = Resource.Success(newData)
            )
        }
    }
    
    fun setInvalidVote(totalInvalidVote: Int){
        _state.update { state ->
            state.copy(
                invalidVote = totalInvalidVote
            )
        }
    }
    
    fun setTermIsApproved(checked: Boolean = true){
        _state.update { state ->
            state.copy(
                termsIsApproved = checked
            )
        }
    }
    
    fun submit(){
        state.value.apply {
            repository.vote(
                tps = tps!!,
                election = election!!,
                candidates = candidatesVote,
                parties = partiesVote,
                invalidVote = invalidVote,
            ).launchCollectLatest { 
                _state.update { state -> state.copy(submitResult = it) }
            }
        }
    }
    
    fun clear(){
        _state.update { state -> 
            state.copy(
                voteData = Resource.Idle(),
                candidatesVote = listOf(),
                partiesVote = listOf(),
                invalidVote = 0,
                termsIsApproved = false,
                tps = null,
                election = null,
                submitResult = Resource.Idle()
            )
        }
    }
}

data class VoteUiState(
    val hasInitState: Boolean = false,
    val voteData: Resource<VoteData> = Resource.Idle(),
    // first Int is ID, second Int is total vote
    val candidatesVote: List<Pair<Int, Int>> = listOf(),
    // first Int is ID, second Int is total vote
    val partiesVote: List<Pair<Int, Int>> = listOf(),
    val invalidVote: Int = 0,
    val termsIsApproved: Boolean = false,
    val tps: TPS? = null,
    val election: Election? = null,
    val submitResult: Resource<BasicMessage> = Resource.Idle(),
    val hasInputData: Boolean = false
)
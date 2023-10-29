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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoteViewModel @Inject constructor(
    private val repository: IVoteRepository
) : BaseViewModel<VoteUiState>() {
    override val _state = MutableStateFlow(VoteUiState())
    
    fun setTpsElection(tps: TPS, election: Election){
        _state.update { state ->
            state.copy(
                tps = tps,
                election = election
            )
        }
        getCandidates()
    }
    
    fun getCandidates(){
        if (state.value.tps != null && state.value.election != null){
            repository.getCandidateList(
                state.value.tps!!,
                state.value.election!!
            ).launchCollectLatest {
                _state.update { state -> state.copy(voteData = it) }
            }
        }
    }
    
    private var updateCandidateVote: Job? = null
    fun setCandidateVote(partyId: Int, candidateId: Int, vote: Int){
        updateCandidateVote?.cancel()
        updateCandidateVote = viewModelScope.launch {
           
            val newCandidatesVote = state.value.candidatesVote.filter{ pair ->
                pair.first != candidateId
            } + (candidateId to vote)
            val newPartyListItem = updateCandidateVote(partyId, candidateId, vote)
            val newVoteData = state.value.voteData.data?.copy(
                partyLists = newPartyListItem,
            )
            _state.update { state ->
                state.copy(
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
                                totalCandidateVote = vote,
                                requestFocus = true
                            )
                        } else {
                            candidate.copy(
                                requestFocus = false
                            )
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
            delay(3000)
            val newPartiesVote = state.value.partiesVote.filter{ pair ->
                pair.first != partyId
            } + (partyId to vote)
            val newPartyListItem = updatePartyVote(partyId, vote)
            val newVoteData = state.value.voteData.data?.copy(
                partyLists = newPartyListItem
            )
            _state.update { state ->
                state.copy(
                    partiesVote = newPartiesVote,
                    voteData = Resource.Success(newVoteData!!)
                )
            }
            // reset focus cursor after 3 seconds delay
//            resetFocusPartyVote()
        }
    }

    private fun updatePartyVote(
        partyId: Int,
        vote: Int,
    ): List<VoteData.PartyListsItem> {
        val newPartyListItem = state.value.voteData.data?.partyLists?.map {
            if (it.id == partyId) {
                it.copy(
                    totalPartyVote = vote,
                    totalVote = vote + it.candidateList.sumOf { candidate ->
                        candidate.totalCandidateVote
                    },
                    requestFocus = true
                )
            } else {
                it.copy(
                    requestFocus = false
                )
            }
        } ?: listOf()
        return newPartyListItem
    }

    private fun resetFocusPartyVote() {
        val newPartyListItem = state.value.voteData.data?.partyLists?.map {
            it.copy(
                requestFocus = false
            )
        } ?: listOf()
        val newVoteData = state.value.voteData.data?.copy(
            partyLists = newPartyListItem
        )
        _state.update { state ->
            state.copy(
                voteData = Resource.Success(newVoteData!!)
            )
        }
    }

    fun toggleView(position: Int, data: VoteData.PartyListsItem){
        val newPartyListsItem = state.value.voteData.data?.partyLists?.map { 
            if (it.id == data.id){
                data.copy(
                    isExpanded = !data.isExpanded
                )
            }else{
                data
            }
        } ?: listOf()
        val newData = state.value.voteData.data!!.copy(
            partyLists = newPartyListsItem
        )
        _state.update { state ->
            state.copy(
                updateThePosition = position,
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
}

data class VoteUiState(
    val voteData: Resource<VoteData> = Resource.Idle(),
    // first Int is ID, second Int is total vote
    val candidatesVote: List<Pair<Int, Int>> = listOf(),
    // first Int is ID, second Int is total vote
    val partiesVote: List<Pair<Int, Int>> = listOf(),
    val invalidVote: Int = 0,
    val termsIsApproved: Boolean = false,
    val tps: TPS? = null,
    val election: Election? = null,
    // Position to be updated in recyclerview
    val updateThePosition: Int? = null,
    val submitResult: Resource<BasicMessage> = Resource.Idle()
)
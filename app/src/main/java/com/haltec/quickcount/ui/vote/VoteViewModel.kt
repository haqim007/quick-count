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
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class VoteViewModel @Inject constructor(
    @ViewModelScoped
    private val repository: IVoteRepository
) : BaseViewModel<VoteUiState>() {
    override val _state = MutableStateFlow(VoteUiState())
    
    init {
        state.map { it.voteData }.distinctUntilChanged()
            .launchCollectLatest {
                val isInitState = !state.value.hasInitState
                val isSuccess = it is Resource.Success
                val isErrorWithData = it is Resource.Error && it.data != null

                if (isInitState && (isSuccess || isErrorWithData)) {
                    setInitState()
                }

            }
        
        state.map { it.candidatesVote }.distinctUntilChanged()
            .debounce(3000)
            .launchCollectLatest {
            if (state.value.hasInitState){
                onCandidatesVoteChange(it)
            }
        }
    }
    
    private fun onCandidatesVoteChange(
        candidatesVote: List<CandidateVoteState>
    ){

        val newPartyListItem = updateCandidateVote(candidatesVote)
        val newVoteData = state.value.voteData.data?.copy(
            partyLists = newPartyListItem,
        )
        _state.update { state ->
            state.copy(
                voteData = Resource.Success(newVoteData!!)
            )
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
                updateCandidatesVoteState(newCandidatesVote, partyListsItem.id, candidateItem.id, candidateItem.totalCandidateVote)
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

    private fun updateCandidatesVoteState(
        currentCandidatesVote: List<CandidateVoteState>, 
        partyId: Int,
        candidateId: Int, 
        vote: Int
    ) =
        currentCandidatesVote.filter { candidateVoteState ->
            candidateVoteState.candidateId != candidateId && candidateVoteState.partyId != partyId
        } + CandidateVoteState(partyId, candidateId, vote)

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
                //dummy
                // use this variable to test list with many candidates
//                val dummyVoteData: Resource<VoteData> = if (it is Resource.Success && it.data != null){
//                    val dummy = it.data.partyLists.map { partyListsItem ->
//                        // dummy
//                        val dummy = mutableListOf<VoteData.Candidate>()
//                        for (i in 99..105){
//                            dummy.add(partyListsItem.candidateList[0].copy(
//                                id = i
//                            ))
//                        }
//                        //dummy
//                        partyListsItem.copy(
//                            candidateList = partyListsItem.candidateList + dummy
//                        )
//                    }
//                    
//                    Resource.Success(
//                        it.data.copy(
//                            partyLists = dummy
//                        )
//                    )
//                }else{
//                    it
//                }
                //dummy
                
                _state.update { state -> 
                    state.copy(voteData = it) 
                }
            }
        }
    }
    
    private var updateCandidateVote: Job? = null
    fun setCandidateVote(partyId: Int, candidateId: Int, vote: Int){
        updateCandidateVote?.cancel()
        val newCandidatesVote = updateCandidatesVoteState(state.value.candidatesVote, partyId, candidateId, vote)
        _state.update { state ->
            state.copy(
                hasInputData =  true,
                candidatesVote = newCandidatesVote
            )
        }
        
    }

    private fun updateCandidateVote(
        candidatesVote: List<CandidateVoteState>
    ): List<VoteData.PartyListsItem> {
        var newPartyListItem = state.value.voteData.data?.partyLists
        candidatesVote.forEach { candidateVote ->
            newPartyListItem = newPartyListItem?.map {partyListItem ->
                if (partyListItem.id == candidateVote.partyId){
                    partyListItem.copy(
                        totalVote = partyListItem.totalPartyVote + partyListItem.candidateList.sumOf { candidate ->
                            if (candidate.id == candidateVote.candidateId) {
                                candidateVote.vote
                            } else {
                                candidate.totalCandidateVote
                            }
                        },
                        candidateList = partyListItem.candidateList.map { candidate ->
                            if (candidate.id == candidateVote.candidateId) {
                                candidate.copy(
                                    totalCandidateVote = candidateVote.vote
                                )
                            } else {
                                candidate
                            }
                        }
                    )
                }else{
                    partyListItem
                }
            } ?: listOf()
        }
        return newPartyListItem ?: listOf()
    }

    private var updatePartyVote: Job? = null
    fun setPartyVote(partyId: Int, vote: Int){
        updatePartyVote?.cancel()
        updatePartyVote = viewModelScope.launch {
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

    private var invalidVoteJob: Job? = null
    fun setInvalidVote(totalInvalidVote: Int){
        invalidVoteJob?.cancel()
        invalidVoteJob = viewModelScope.launch {
            _state.update { state ->
                state.copy(
                    invalidVote = totalInvalidVote
                )
            }
            delay(3000)
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
                candidates = candidatesVote.map { 
                   Pair(it.candidateId, it.vote)
                },
                parties = if (voteData.data?.isParty == true) partiesVote else null ,
                invalidVote = invalidVote,
                isParty = voteData.data?.isParty == true
            ).launchCollectLatest { 
                _state.update { state -> state.copy(submitResult = it) }
            }
        }
    }
    
    fun clear(){
        _state.update { state -> 
            state.copy(
                hasInitState = false,
                hasInputData = false,
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
    // contains candidates that are being inputted
    val onEditCandidates: List<VoteData.Candidate> = listOf(),
    
    // contains inputted candidate's vote
    val candidatesVote: List<CandidateVoteState> = listOf(),
    // first Int is ID, second Int is total vote
    val partiesVote: List<Pair<Int, Int>> = listOf(),
    val invalidVote: Int = 0,
    val termsIsApproved: Boolean = false,
    val tps: TPS? = null,
    val election: Election? = null,
    val submitResult: Resource<BasicMessage> = Resource.Idle(),
    val hasInputData: Boolean = false
)

data class CandidateVoteState(
    val partyId: Int,
    val candidateId: Int,
    val vote: Int
)
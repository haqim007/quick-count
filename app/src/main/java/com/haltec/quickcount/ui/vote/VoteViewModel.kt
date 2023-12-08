package com.haltec.quickcount.ui.vote

import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList
import javax.inject.Inject

typealias CandidateId = Int

@OptIn(FlowPreview::class)
@HiltViewModel
class VoteViewModel @Inject constructor(
    @ViewModelScoped
    private val repository: IVoteRepository
) : BaseViewModel<VoteUiState>() {
    override val _state = MutableStateFlow(VoteUiState())
    private var hasInitState: Boolean = false
    
    init {
        viewModelScope.launch {
            state.map { it.voteData }.distinctUntilChanged()
                .collectLatest {
                    val isInitState = !hasInitState
                    val isSuccess = it is Resource.Success
                    val isErrorWithData = it is Resource.Error && it.data != null
                    Log.d(this@VoteViewModel.javaClass.name, "$isInitState $isSuccess $isErrorWithData")
                    if (isInitState && (isSuccess || isErrorWithData)) {
                        setInitState()
                    }

                }
        }
        
        state.map { it.candidatesVote }.distinctUntilChanged()
            .debounce(3000)
            .launchCollectLatest {
            if (hasInitState){
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
    
    fun setInitState(){
        viewModelScope.launch {
            var partiesVote = state.value.partiesVote
            val newCandidatesVote = state.value.candidatesVote
            state.value.voteData.data?.partyLists?.forEach { partyListsItem ->
                partiesVote = updatePartiesVoteState(partiesVote, partyListsItem.id, partyListsItem.totalPartyVote)

                partyListsItem.candidateList.forEach { candidateItem ->
                    updateCandidatesVoteState(newCandidatesVote, partyListsItem.id, candidateItem.id, candidateItem.totalCandidateVote)
                }
            }

            hasInitState = true
            Log.d(this@VoteViewModel.javaClass.name, "hasInitState: $hasInitState")
            _state.update { state ->
                state.copy(
                    invalidVote = state.voteData.data?.invalidVote ?: 0,
                    partiesVote = partiesVote,
                    candidatesVote = newCandidatesVote,
                )
            }
        }
    }

    private fun updateCandidatesVoteState(
        currentCandidatesVote: List<CandidateVoteState>, 
        partyId: Int,
        candidateId: Int, 
        vote: Int
    ): List<CandidateVoteState> {
        
        val currentCandidatesVoteMap = currentCandidatesVote.associateBy { it.candidateId }.toMutableMap()

        currentCandidatesVoteMap[candidateId] = CandidateVoteState(partyId, candidateId, vote)
        
        return currentCandidatesVoteMap.values.toList()
        
    }
        

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
                //val dataDummy = generateDummyCandidate(it, it.data)
                //dummy
                
                _state.update { state -> 
                    state.copy(voteData = it) 
                }
            }
        }
    }

    // use this to test list with many candidates
    private fun generateDummyCandidate(
        it: Resource<VoteData>,
        data: VoteData,
    ): Resource<VoteData> {
        return if (it is Resource.Success && it.data != null) {
            val dummy = data.partyLists.map { partyListsItem ->
                // dummy
                val dummy = mutableListOf<VoteData.Candidate>()
                for (i in 99..105) {
                    dummy.add(
                        partyListsItem.candidateList[0].copy(
                            id = i
                        )
                    )
                }
                //dummy
                partyListsItem.copy(
                    candidateList = partyListsItem.candidateList + dummy
                )
            }

            Resource.Success(
                data.copy(
                    partyLists = dummy
                )
            )
        } else {
            it
        }
    }

    private var updateCandidateVote: Job? = null
    // Pair<PartyId, CandidateId>
    private var lastCandidateUpdate: Pair<Int, Int>? = null
    fun setCandidateVote(partyId: Int, candidateId: Int, vote: Int){
        val candidateVotes = state.value.candidatesVote
        val newCandidateVotes = updateCandidatesVoteState(candidateVotes, partyId, candidateId, vote)
        _state.update { state ->
            state.copy(
                hasInputData =  true,
                candidatesVote = newCandidateVotes
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
            .map { 
                it.voteData.data?.partyLists?.first { 
                    it.id == partyId 
                }?.totalVote ?: 0 
            }
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
        hasInitState = false
        _state.update { state -> 
            state.copy(
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
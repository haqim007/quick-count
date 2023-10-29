package com.haltec.quickcount.ui.tpselectionlist

import androidx.lifecycle.viewModelScope
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.TPSElection
import com.haltec.quickcount.domain.repository.ITPSRepository
import com.haltec.quickcount.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TPSElectionListViewModel @Inject constructor(
    private val repository: ITPSRepository
) : BaseViewModel<TPSElectionListUiState>() {
    override val _state = MutableStateFlow(TPSElectionListUiState())
    
    init {
        viewModelScope.launch {
            setFilter()
            state.map { it.filter }.distinctUntilChanged().collectLatest {
                getTPSElectionlist()
            }
        }
    }
    
    fun setFilter(value: String = ""){
        _state.update { state ->
            state.copy(
                filter = value
            )
        }
        getTPSElectionlist()
    }
    
    fun getTPSElectionlist(){
        repository.getTPSElections(state.value.filter).launchCollectLatest {
            _state.update { state -> state.copy(data = it) }
        }
    }

}

data class TPSElectionListUiState(
    val filter: String = "",
    val data: Resource<List<TPSElection>> = Resource.Idle(),
)
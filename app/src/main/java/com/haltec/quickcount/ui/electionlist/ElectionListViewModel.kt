package com.haltec.quickcount.ui.electionlist

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.repository.IElectionRepository
import com.haltec.quickcount.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ElectionListViewModel @Inject constructor(
    private val repository: IElectionRepository
) : BaseViewModel<ElectionListUiState>() {
    override val _state = MutableStateFlow(ElectionListUiState())
    val pagingFlow: Flow<PagingData<Election>>
        get() = _pagingFlow
    private var _pagingFlow: Flow<PagingData<Election>>
    
    init {
        _pagingFlow = flowOf(PagingData.empty())

        viewModelScope.launch {
            state.map { it.tps }.distinctUntilChanged().collectLatest {
                it?.let {
                    _pagingFlow = getElectionlist().cachedIn(viewModelScope)
                }
            }
        }
    }
    
    fun setTps(tps: TPS){
        _state.update { state ->
            state.copy(
                tps = tps
            )
        }
    }
    
    private fun getElectionlist(): Flow<PagingData<Election>>{
        return state.value.tps?.id?.let { repository.getElectionList(it) } ?: flowOf(PagingData.empty())
    }
}

data class ElectionListUiState(
    val tps: TPS? = null,
)
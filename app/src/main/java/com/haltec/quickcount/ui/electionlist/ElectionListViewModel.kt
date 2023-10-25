package com.haltec.quickcount.ui.electionlist

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.Election
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.repository.IElectionRepository
import com.haltec.quickcount.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
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
    
    init {
        viewModelScope.launch {
            state.map { it.tps }.distinctUntilChanged().collectLatest {
                it?.let {
                    getElectionlist()
                    Log.d("http hehe", it.name)
                }
            }
        }
    }
    
    fun setTps(tps: TPS){
        Log.d("http hihi", tps.name)
        _state.update { state ->
            state.copy(
                tps = tps
            )
        }
    }
    
    fun getElectionlist(){
        state.value.tps?.id?.let {
            repository.getElectionList(it).launchCollectLatest {
                _state.update { state -> state.copy(data = it) }
            }
        } ?: run {
            _state.update { state -> state.copy(data = Resource.Success(listOf())) }
        }
    }
}

data class ElectionListUiState(
    val tps: TPS? = null,
    val data: Resource<List<Election>> = Resource.Idle()
)
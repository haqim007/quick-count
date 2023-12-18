package com.haltec.quickcount.ui.tpselectionlist

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.TPSElection
import com.haltec.quickcount.domain.model.stringToElectionFilter
import com.haltec.quickcount.domain.repository.ITPSElectionRepository
import com.haltec.quickcount.domain.repository.ITPSRepository
import com.haltec.quickcount.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TPSElectionListViewModel @Inject constructor(
    private val repository: ITPSElectionRepository
) : BaseViewModel<TPSElectionListUiState>() {
    
    override val _state = MutableStateFlow(TPSElectionListUiState())
    private val _pagingFlow = MutableStateFlow<PagingData<TPSElection>>(PagingData.empty())
    val pagingFlow: Flow<PagingData<TPSElection>> = _pagingFlow.asStateFlow()
    
    init {
        setFilter()
        viewModelScope.launch {
            state.map { it.filter }
                .distinctUntilChanged()
                .flatMapLatest { filter ->
                    repository.getTPSElections(stringToElectionFilter(filter)).cachedIn(viewModelScope)
                }.collectLatest { pagingData ->
                    _pagingFlow.emit(pagingData)
                }
        }

    }
    
    fun setFilter(value: String = ""){
        _state.update { state ->
            state.copy(
                filter = value
            )
        }
    }

}

data class TPSElectionListUiState(
    val filter: String = ""
)
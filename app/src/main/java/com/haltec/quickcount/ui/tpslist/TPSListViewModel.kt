package com.haltec.quickcount.ui.tpslist

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.repository.ITPSRepository
import com.haltec.quickcount.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TPSListViewModel @Inject constructor(
    private val repository: ITPSRepository
): BaseViewModel<TPSListUiState>() {
    override val _state = MutableStateFlow(TPSListUiState())
    val pagingFlow: Flow<PagingData<TPS>>
    
    init {
        getUserName()
        
        pagingFlow = repository.getTPSList().cachedIn(viewModelScope)
    }
    
    private fun getUserName() {
        viewModelScope.launch {
            _state.update { state ->
                state.copy(
                    userName = repository.getUsername()
                )
            }
        }
    }

}



data class TPSListUiState(
    val userName: String? = null
)
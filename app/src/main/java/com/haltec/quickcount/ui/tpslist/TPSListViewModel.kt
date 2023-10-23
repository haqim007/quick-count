package com.haltec.quickcount.ui.tpslist

import androidx.lifecycle.viewModelScope
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.repository.ITPSRepository
import com.haltec.quickcount.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TPSListViewModel @Inject constructor(
    private val repository: ITPSRepository
): BaseViewModel<TPSListUiState>() {
    override val _state = MutableStateFlow(TPSListUiState())
    
    init {
        getUserName()
        getTPSList()  
    }
    
    fun getTPSList(){
        repository.getTPSList().launchCollectLatest { 
            _state.update { state ->
                state.copy(
                    data = it
                )
            }
        }
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
    val data:  Resource<List<TPS>> = Resource.Idle(),
    val userName: String? = null
)
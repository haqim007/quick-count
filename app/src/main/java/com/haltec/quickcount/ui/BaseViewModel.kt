package com.haltec.quickcount.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haltec.quickcount.ui.login.LoginUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

abstract class BaseViewModel<UiState>: ViewModel() {

    protected abstract val _state: MutableStateFlow<UiState>
    val state get() = _state.asStateFlow()
    
    fun <T> Flow<T>.launchCollectLatest(callback: (value: T) -> Unit){
        viewModelScope.launch { 
            this@launchCollectLatest.distinctUntilChanged().collectLatest { 
                callback(it)
            }
        }
    }

    fun <T> Flow<T>.launchCollect(callback: (value: T) -> Unit){
        viewModelScope.launch {
            this@launchCollect.distinctUntilChanged().collect {
                callback(it)
            }
        }
    }
}
package com.haltec.quickcount.ui

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.haltec.quickcount.domain.repository.ILoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val loginRepository: ILoginRepository
): BaseViewModel<MainUiState>() {
    override val _state = MutableStateFlow(MainUiState())
    
    init {
        checkSession()
    }
    
    fun setPermissionLocationGrant(isGranted: Boolean){
        _state.update { state ->
            state.copy(
                isPermissionLocationGranted = isGranted
            )
        }
    }
    
    fun showLocationPermissionDialog(){
        _state.update { state ->
            state.copy(
                showLocationPermissionDialog = true
            )
        }
        hideLocationPermissionDialog()
    }

    private fun hideLocationPermissionDialog(){
        _state.update { state ->
            state.copy(
                showLocationPermissionDialog = false
            )
        }
    }
    
    private fun checkSession(){
        loginRepository.checkSessionValid().launchCollect {
            _state.update { state ->
                state.copy(
                    sessionValid = it
                )
            }
        }
    }
    
    fun logout(){
        viewModelScope.launch {
            loginRepository.logout()
        }
    }
    
}

data class MainUiState(
    val isPermissionLocationGranted: Boolean? = null,
    val showLocationPermissionDialog: Boolean = false,
    val sessionValid: Boolean? = null,
)
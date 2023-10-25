package com.haltec.quickcount.ui

import androidx.lifecycle.viewModelScope
import com.haltec.quickcount.domain.model.SessionValidity
import com.haltec.quickcount.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val loginRepository: IAuthRepository
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
    
    fun requestToLogout(){
        _state.update { state -> state.copy(requestToLogout = true) }
    }

    fun logout(){
        viewModelScope.launch {
            loginRepository.logout()
        }
        _state.update { state -> state.copy(requestToLogout = false) }
    }
    
    fun cancelLogout(){
        _state.update { state -> state.copy(requestToLogout = false) }
    }
    
}

data class MainUiState(
    val isPermissionLocationGranted: Boolean? = null,
    val showLocationPermissionDialog: Boolean = false,
    val sessionValid: SessionValidity? = null,
    val requestToLogout: Boolean = false
)
package com.haltec.quickcount.ui.login

import dagger.hilt.android.lifecycle.HiltViewModel
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.Login
import com.haltec.quickcount.domain.repository.IAuthRepository
import com.haltec.quickcount.ui.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: IAuthRepository
): BaseViewModel<LoginUiState>() {
    override val _state = MutableStateFlow(LoginUiState())
    
    init {
        getDeviceToken()
    }
    
    private fun getDeviceToken(){
        loginRepository.getDeviceToken().launchCollectLatest {
            _state.update { state ->
                state.copy(
                    deviceToken = it
                )
            }
        }
    }
    
    suspend fun saveDeviceToken(key: String){
        loginRepository.storeDeviceToken(key)
    }
    
    fun login(username: String, password: String){
        val inputIsValid = validate(username, password) 
        if(inputIsValid){
            loginRepository.login(username, password).launchCollect {
                _state.update { state ->
                    state.copy(
                        result = it
                    )
                }
            }
        }
    }
    
    private fun validate(username: String, password: String): Boolean {
        _state.update { state ->
            state.copy(
                usernameIsValid = username.isNotBlank(),
                passwordIsValid = password.isNotBlank()
            )
        }
        return username.isNotBlank() && password.isNotBlank()
    }
    
}

data class LoginUiState(
    val deviceToken: String? = null,
    val result: Resource<Login> = Resource.Idle(),
    val usernameIsValid: Boolean = true,
    val passwordIsValid: Boolean = true
)
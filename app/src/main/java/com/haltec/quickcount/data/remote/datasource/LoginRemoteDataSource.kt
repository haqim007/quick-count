package com.haltec.quickcount.data.remote.datasource

import dagger.hilt.android.scopes.ViewModelScoped
import com.haltec.quickcount.data.mechanism.getResult
import com.haltec.quickcount.data.remote.request.LoginRequest
import com.haltec.quickcount.data.remote.service.AuthService
import javax.inject.Inject

@ViewModelScoped
class LoginRemoteDataSource @Inject constructor(
    private val authService: AuthService
) {
    suspend fun login(loginRequest: LoginRequest) = 
        getResult { 
            authService.login(loginRequest)
        }
}
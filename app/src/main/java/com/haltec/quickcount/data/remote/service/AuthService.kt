package com.haltec.quickcount.data.remote.service

import com.haltec.quickcount.data.remote.request.LoginRequest
import com.haltec.quickcount.data.remote.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): LoginResponse

}
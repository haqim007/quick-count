package com.haltec.quickcount.domain.repository

import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.Login
import com.haltec.quickcount.domain.model.SessionValidity
import kotlinx.coroutines.flow.Flow


interface IAuthRepository {
    suspend fun storeDeviceToken(token: String)
    fun getDeviceToken(): Flow<String?>
    fun login(username: String, password: String): Flow<Resource<Login>>
    fun checkSessionValid(): Flow<SessionValidity>
    suspend fun logout()
}
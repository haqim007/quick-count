package com.haltec.quickcount.domain.repository

import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.Login
import kotlinx.coroutines.flow.Flow


interface ILoginRepository {
    suspend fun storeDeviceToken(token: String)
    fun getDeviceToken(): Flow<String?>
    fun login(username: String, password: String): Flow<Resource<Login>>
    fun checkSessionValid(): Flow<Boolean>
    suspend fun logout()
}
package com.haltec.quickcount.data.repository

import com.haltec.quickcount.data.local.room.AppDatabase
import dagger.hilt.android.scopes.ViewModelScoped
import com.haltec.quickcount.data.mechanism.NetworkBoundResource
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.preference.DevicePreference
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.datasource.LoginRemoteDataSource
import com.haltec.quickcount.data.remote.request.LoginRequest
import com.haltec.quickcount.data.remote.response.LoginResponse
import com.haltec.quickcount.di.DispatcherIO
import com.haltec.quickcount.domain.model.Login
import com.haltec.quickcount.domain.model.SessionValidity
import com.haltec.quickcount.domain.repository.IAuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class AuthRepository @Inject constructor(
    private val devicePreference: DevicePreference,
    private val userPreference: UserPreference,
    private val remoteDataSource: LoginRemoteDataSource,
    private val database: AppDatabase,
    @DispatcherIO
    private val dispatcher: CoroutineDispatcher
) : IAuthRepository {

    override fun checkSessionValid(): Flow<SessionValidity> {
        return userPreference.getSessionValidity()
    }

    override suspend fun storeDeviceToken(token: String) {
        devicePreference.storeDeviceToken(token)
    }

    override fun getDeviceToken(): Flow<String?> {
        return devicePreference.getDeviceToken()
    }

    override fun login(username: String, password: String): Flow<Resource<Login>> {
        return object: NetworkBoundResource<Login, LoginResponse>(){
            
            override suspend fun requestFromRemote(): Result<LoginResponse> {
                val deviceToken = devicePreference.getDeviceToken().first() ?: ""
                return remoteDataSource.login(
                    LoginRequest(username, password, deviceToken)
                )
            }

            override suspend fun onSuccess(data: LoginResponse) {
                userPreference.storeUserInfo(userInfo = data.toUserInfo())
            }

            override fun loadResult(data: LoginResponse): Flow<Login> {
                return flow { 
                    emit(data.toModel())
                }
            }
        }.asFlow().flowOn(dispatcher)
    }

    override suspend fun logout() {
        
        withContext(dispatcher){
            userPreference.resetUserInfo(true)
            devicePreference.reset()
            database.clearAllTables()
        }
        
    }
}
package com.haltec.quickcount.data.repository

import android.util.Log
import dagger.hilt.android.scopes.ViewModelScoped
import com.haltec.quickcount.data.mechanism.NetworkBoundResource
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.data.preference.DevicePreference
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.datasource.LoginRemoteDataSource
import com.haltec.quickcount.data.remote.request.LoginRequest
import com.haltec.quickcount.data.remote.response.LoginResponse
import com.haltec.quickcount.data.util.capitalizeWords
import com.haltec.quickcount.data.util.currentTimestamp
import com.haltec.quickcount.data.util.stringToTimestamp
import com.haltec.quickcount.di.DispatcherIO
import com.haltec.quickcount.domain.model.Login
import com.haltec.quickcount.domain.model.UserInfo
import com.haltec.quickcount.domain.repository.ILoginRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.log

@ViewModelScoped
class LoginRepository @Inject constructor(
    private val devicePreference: DevicePreference,
    private val userPreference: UserPreference,
    private val remoteDataSource: LoginRemoteDataSource,
    @DispatcherIO
    private val dispatcher: CoroutineDispatcher
) : ILoginRepository {

    override fun checkSessionValid(): Flow<Boolean> {
        return userPreference.getUserInfo().map {   
//            it.expiredTimestamp?.let {expiredTimestamp ->
//                expiredTimestamp > currentTimestamp()
//            } ?: false || 
            !it.token.isNullOrEmpty() || it.name.isNotEmpty()
        }
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

            override suspend fun onFetchSuccess(data: LoginResponse) {
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
        userPreference.resetUserInfo(true)
    }
}
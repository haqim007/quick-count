package com.haltec.quickcount.data.mechanism

import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.domain.model.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map


/*
*
* RequestType: Data type that used to catch network response a.k.a inserted data type
* ResultType: Data type that expected as return data a.k.a output data type
* */
abstract class NetworkBoundResource<ResultType, RequestType> {

    private val result: Flow<Resource<ResultType>> = flow{
        emit(Resource.Loading())
        try {
            val currentLocalData = loadCurrentLocalData()
            if(onBeforeRequest()){
                val apiResponse = requestFromRemote()
                if(apiResponse.isSuccess){
                    apiResponse.getOrNull()?.let { res ->
                        onFetchSuccess(res)
                        emitAll(
                            loadResult(res).map {
                                Resource.Success(it)
                            }
                        )
                    }

                }else{
                    onFetchFailed(apiResponse.exceptionOrNull() as CustomThrowable)
                    emit(
                        Resource.Error(
                            message = apiResponse
                                .exceptionOrNull()
                                ?.localizedMessage ?: "Unknown error",
                            data = currentLocalData
                        )
                    )
                }
            }else{
                emit( Resource.Error(message = "Request is not allowed") )
            }
            
        }catch (e: Exception){
            onFetchFailed()
            emit(
                Resource.Error(
                    message = e.localizedMessage ?: "Unknown error"
                )
            )
        }
    }

    protected abstract suspend fun requestFromRemote(): Result<RequestType>

    /**
     * Load from network to be returned and consumed. Convert data from network to model here
     *
     * @param data
     * @return
     */
    protected abstract fun loadResult(data: RequestType): Flow<ResultType>

    /**
     * Load current data from local storage
     *
     */
    protected open suspend fun loadCurrentLocalData(): ResultType? = null

    protected open suspend fun onFetchSuccess(data: RequestType) {}

    protected open suspend fun onFetchFailed(exceptionOrNull: CustomThrowable? = null) {}
    
    /*
    * 
    * Will be called just before [requestFromRemote] triggered in case what to perform task 
    * to determine whether to perform [requestFromRemote] or not
    * 
    * */
    protected open suspend fun onBeforeRequest(): Boolean = true
    
    fun asFlow(): Flow<Resource<ResultType>> = result

}

abstract class AuthorizedNetworkBoundResource<ResultType, RequestType>(
    private val userPreference: UserPreference
): NetworkBoundResource<ResultType, RequestType>(){
    
    override suspend fun onBeforeRequest(): Boolean {
        val currentTimestampInSeconds = System.currentTimeMillis() / 1000
        var tokenIsValid = false
        userPreference.getUserInfo().first().expiredTimestamp?.let {
            tokenIsValid = it > currentTimestampInSeconds
            if (it < currentTimestampInSeconds){
                userPreference.resetUserInfo()
            }
        }
        return tokenIsValid
    }

    override suspend fun onFetchFailed(exceptionOrNull: CustomThrowable?) {
        if(exceptionOrNull?.code == 401){
            userPreference.resetUserInfo()
        }
    }
    
}
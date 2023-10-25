package com.haltec.quickcount.data.mechanism

import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.util.DATE_TIME_FORMAT
import com.haltec.quickcount.data.util.stringToTimestamp
import com.haltec.quickcount.domain.model.UserInfo
import kotlinx.coroutines.delay
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
                val apiResponse = requestFromRemoteRunner()
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
                                ?.localizedMessage ?: "Failed to fetch data",
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
                    message = e.localizedMessage ?: "Failed to request"
                )
            )
        }
    }

    /**
     * To handle how requestFromRemote will be executed
     *
     * @return
     */
    protected open suspend fun requestFromRemoteRunner(): Result<RequestType>{
        return requestFromRemote()
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
    * Will be called just before [requestFromRemote] triggered in case to perform task 
    * to determine whether to perform [requestFromRemote] or not
    * 
    * */
    protected open suspend fun onBeforeRequest(): Boolean = true
    
    fun asFlow(): Flow<Resource<ResultType>> = result

}

abstract class AuthorizedNetworkBoundResource<ResultType, RequestType>(
    private val userPreference: UserPreference
): NetworkBoundResource<ResultType, RequestType>(){

    final override suspend fun requestFromRemoteRunner(): Result<RequestType> {
        var apiResponse: Result<RequestType>? = null
        
        // Repeat three times in case request failed because of http code 401.
        // Because from the network API when the token expired, 
        // it will response refreshed token instead of just (error) message
        run repeatBlock@{
            repeat(3){
                apiResponse = requestFromRemote()
                if (apiResponse?.isSuccess == false){
                    val exception = apiResponse?.exceptionOrNull() as CustomThrowable
                    if (exception.code == 401 && exception.data?.data != null){
                        userPreference.updateToken(
                            exception.data.data.token,
                            stringToTimestamp(exception.data.data.exp, DATE_TIME_FORMAT) ?: 0
                        )
                        delay(3000)
                    }else{
                        return@repeatBlock
                    }
                }else{
                    return@repeatBlock
                }
            }
        }
        
        
        return apiResponse!!
    }

    override suspend fun onFetchFailed(exceptionOrNull: CustomThrowable?) {
        if(exceptionOrNull?.code == 401){
            userPreference.resetUserInfo()
        }
    }
    
}
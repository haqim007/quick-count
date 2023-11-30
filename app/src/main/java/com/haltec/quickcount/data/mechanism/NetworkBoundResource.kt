package com.haltec.quickcount.data.mechanism

import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.data.remote.base.checkToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map


/*
*
* RequestType: Data type that used to catch network response a.k.a inserted data type
* ResultType: Data type that expected as return data a.k.a output data type
* */
abstract class NetworkBoundResource<ResultType, ResponseType> {

    private val result: Flow<Resource<ResultType>> = flow{
        emit(Resource.Loading())
        try {
            val currentLocalData = loadCurrentLocalData()
            if(onBeforeRequest()){
                val apiResponse = requestFromRemoteRunner()
                if(apiResponse.isSuccess){
                    apiResponse.getOrNull()?.let { res ->
                        onSuccess(res)
                        emitAll(
                            loadResult(res).map {
                                Resource.Success(it)
                            }
                        )
                    }

                }else{
                    val mException = apiResponse.exceptionOrNull() as? CustomThrowable
                    
                    if (mException != null){
                        onFailed(mException)
                        emit(
                            Resource.Error(
                                message = mException.message ?: "Failed to fetch data",
                                data = currentLocalData,
                                code = mException.code
                            )
                        )
                    }else{
                        val exceptionFallback = apiResponse.exceptionOrNull()
                        emit(
                            Resource.Error(
                                message = exceptionFallback?.message ?: "Failed to fetch data",
                                data = currentLocalData
                            )
                        )
                    }
                    
                }
            }else{
                emit( Resource.Error(message = "Request is not allowed") )
            }
            
        }catch (e: Exception){
            onFailed()
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
    protected open suspend fun requestFromRemoteRunner(): Result<ResponseType>{
        return requestFromRemote()
    }

    protected abstract suspend fun requestFromRemote(): Result<ResponseType>

    /**
     * Load from network to be returned and consumed. Convert data from network to model here
     *
     * @param data
     * @return
     */
    protected abstract fun loadResult(data: ResponseType): Flow<ResultType>

    /**
     * Load current data from local storage
     *
     */
    protected open suspend fun loadCurrentLocalData(): ResultType? = null

    protected open suspend fun onSuccess(data: ResponseType) {}

    protected open suspend fun onFailed(exceptionOrNull: CustomThrowable? = null) {}
    
    /*
    * 
    * Will be called just before [requestFromRemote] triggered in case to perform task 
    * to determine whether to perform [requestFromRemote] or not
    * 
    * */
    protected open suspend fun onBeforeRequest(): Boolean = true
    
    fun asFlow(): Flow<Resource<ResultType>> = result

}

abstract class AuthorizedNetworkBoundResource<ResultType, ResponseType>(
    private val userPreference: UserPreference
): NetworkBoundResource<ResultType, ResponseType>(){

    final override suspend fun requestFromRemoteRunner(): Result<ResponseType> {
        return checkToken(userPreference, requestFromRemote = { requestFromRemote() })
    }

    override suspend fun onFailed(exceptionOrNull: CustomThrowable?) {
        if(exceptionOrNull?.code == 401){
            userPreference.resetUserInfo()
        }
    }
    
}
package com.haltec.quickcount.data.remote.base

import com.haltec.quickcount.data.mechanism.CustomThrowable
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.util.DATE_TIME_FORMAT
import com.haltec.quickcount.util.stringToTimestamp
import kotlinx.coroutines.delay

suspend fun <RequestType> checkToken(
    userPreference: UserPreference,
    requestFromRemote: suspend () -> Result<RequestType>
): Result<RequestType>{
    var apiResponse: Result<RequestType>? = null

    // Repeat three times in case request failed because of http code 401.
    // Because from the network API when the token expired, 
    // it will response refreshed token instead of just (error) message
    run repeatBlock@{
        repeat(3){
            apiResponse = requestFromRemote()
            if (apiResponse?.isSuccess == false){
                val exception = apiResponse?.exceptionOrNull() as? CustomThrowable
                if (exception?.code == 401 && exception.data?.data != null){
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
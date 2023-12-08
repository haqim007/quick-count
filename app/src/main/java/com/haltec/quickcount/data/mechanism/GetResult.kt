package com.haltec.quickcount.data.mechanism

import com.google.gson.Gson
import com.haltec.quickcount.data.mechanism.CustomThrowable.Companion.UNKNOWN_HOST_EXCEPTION
import com.haltec.quickcount.data.remote.response.BasicResponse
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException

suspend fun <T> getResult(callback: suspend () -> T): Result<T> {
    return try {
        Result.success(callback())
    }
    catch (e: UnknownHostException){
        val message = e.message ?: ""
        if (message.contains("failed to connect")) {
            // Handle an unavailable network (e.g., airplane mode, Wi-Fi turned off)
            Result.failure(CustomThrowable(message = "Unavailable network", code = UNKNOWN_HOST_EXCEPTION))
        } else {
            // Handle an unknown host error (e.g., server name cannot be resolved)
            Result.failure(CustomThrowable(message = "Unknown to resolve host", code = UNKNOWN_HOST_EXCEPTION))
        }
        
    }
    catch (e: HttpException){
        val response = e.toErrorMessage()
        Result.failure(CustomThrowable(code = e.code(), message = response.message, data = response))
    }
    catch (e: ConnectException){
        Result.failure(CustomThrowable(message = "Unable to connect"))
    }
    catch (e: Exception){
        Result.failure(e)
    }
}

fun HttpException.toErrorMessage(): BasicResponse {
    val errorJson = this.response()?.errorBody()?.string()
    return if (errorJson.isNullOrEmpty()){
        BasicResponse(
            message = "Internal server error"
        )
    }else{
        val gson = Gson()
        gson.fromJson(errorJson, BasicResponse::class.java)
    }
}

class CustomThrowable(val code: Int = DEFAULT_CODE, override val message: String?, val data: BasicResponse? = null) : Throwable(message){
    companion object{
        const val DEFAULT_CODE = 999
        const val UNKNOWN_HOST_EXCEPTION = 503
        const val NOT_FOUND = 404
    }
}
package com.haltec.quickcount.data.mechanism

import com.google.gson.Gson
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
            Result.failure(CustomThrowable(message = "Unavailable network"))
        } else {
            // Handle an unknown host error (e.g., server name cannot be resolved)
            Result.failure(CustomThrowable(message = "Unknown to resolve host"))
        }
        
    }
    catch (e: HttpException){
        Result.failure(CustomThrowable(code = e.code(), message = e.toErrorMessage()))
    }
    catch (e: ConnectException){
        Result.failure(CustomThrowable(message = "Unable to connect"))
    }
    catch (e: Exception){
        Result.failure(e)
    }
}

fun HttpException.toErrorMessage(): String {
    val errorJson = this.response()?.errorBody()?.string()
    val gson = Gson()
    return gson.fromJson(errorJson, BasicResponse::class.java).message
}

class CustomThrowable(val code: Int = 999, override val message: String?) : Throwable(message)
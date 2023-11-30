package com.haltec.quickcount.data.mechanism

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val code: Int? = null
) {
    class Success<T>(data: T): Resource<T>(data)
    class Loading<T>(data: T? = null): Resource<T>(data)
    class Error<T>(message: String, data: T? = null, code: Int? = null): Resource<T>(data, message, code)
    class Idle<T>: Resource<T>()
}
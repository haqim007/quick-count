package com.haltec.quickcount.data.mechanism


fun <T> Resource<T>.handle(handler: ResourceHandler<T>){
    when (this) {
        is Resource.Success -> handler.onSuccess(this.data)
        is Resource.Error -> handler.onError(this.message, this.data, this.code)
        is Resource.Loading -> handler.onLoading()
        else -> handler.onIdle()
    }
    handler.onAll(this)
}

interface ResourceHandler<T> {
    fun onSuccess(data: T?)
    fun onError(message: String?, data: T? = null, code: Int? = null)
    fun onLoading(){}
    fun onIdle(){}
    fun onAll(resource: Resource<T>){}
}

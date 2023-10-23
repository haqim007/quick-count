package com.haltec.quickcount.data.remote.request

data class LoginRequest(
    val username: String,
    val password: String,
    val token: String
)

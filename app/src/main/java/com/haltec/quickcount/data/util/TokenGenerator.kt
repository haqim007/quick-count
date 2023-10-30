package com.haltec.quickcount.data.util

import com.haltec.quickcount.BuildConfig
import java.security.MessageDigest
import java.util.*

fun generateToken(salt: String): String {
    val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    val randomString = List(16) { charset.random() }.joinToString("")
    val hashed = hashString("SHA-256", randomString + salt).split("")
    return List(16){hashed.random()}.joinToString("")
}

fun hashString(type: String, input: String): String {
    val bytes = MessageDigest
        .getInstance(type)
        .digest(input.toByteArray())
    return bytes.fold("") { str, it -> str + "%02x".format(it) }
}

fun generateDeviceToken(): String{
    val salt = UUID.randomUUID().toString()
    val token = if(BuildConfig.DEBUG) "1234567890" else generateToken(salt)
    return token
}

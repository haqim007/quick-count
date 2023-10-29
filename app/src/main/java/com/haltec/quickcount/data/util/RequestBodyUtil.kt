package com.haltec.quickcount.data.util

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

fun textPlainRequestBody(value: String): RequestBody {
    return value.toRequestBody("text/plain".toMediaTypeOrNull())
}

fun textPlainRequestBodyNullable(value: String?): RequestBody? {
    return value?.toRequestBody("text/plain".toMediaTypeOrNull())
}

fun fileRequestBody(file: File, type: String = "image/*"): RequestBody {
    return file.asRequestBody(type.toMediaTypeOrNull())
}

fun multipartRequestBody(file: File, type: String = "image/*", name: String): MultipartBody.Part {
    return MultipartBody.Part.createFormData(
        name,
        file.name,
        fileRequestBody(file, type)
    )
}
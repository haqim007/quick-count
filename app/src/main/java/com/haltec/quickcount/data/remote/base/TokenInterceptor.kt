package com.haltec.quickcount.data.remote.base

import dagger.hilt.android.scopes.ViewModelScoped
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

@ViewModelScoped
class TokenInterceptor @Inject constructor(
    private val userPreference: UserPreference,
    @ApplicationScope
    private val applicationScope: CoroutineScope
): Interceptor {
    private lateinit var token: String
    init {
        userPreference.getUserInfo().onEach {
            it.token?.let {
                token = it
            }
        }.launchIn(applicationScope)
    }
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}
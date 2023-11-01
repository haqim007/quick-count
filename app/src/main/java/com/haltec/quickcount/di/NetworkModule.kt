package com.haltec.quickcount.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import com.haltec.quickcount.data.remote.base.ApiConfig
import com.haltec.quickcount.data.remote.service.AuthService
import com.haltec.quickcount.data.remote.base.TokenInterceptor
import com.haltec.quickcount.data.remote.service.ElectionService
import com.haltec.quickcount.data.remote.service.TPSService
import com.haltec.quickcount.data.remote.service.UploadEvidenceService
import com.haltec.quickcount.data.remote.service.VoteService
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthorizedOkHttpClient

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class UnauthorizedOkHttpClient

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthorizedApiConfig

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class UnauthorizedApiConfig

@Module
@InstallIn(ViewModelComponent::class)
object NetworkModule {
    
    @AuthorizedOkHttpClient
    @ViewModelScoped
    @Provides
    fun provideOkHttpClient(
        tokenInterceptor: TokenInterceptor
    ): OkHttpClient.Builder = OkHttpClient.Builder()
        .addInterceptor(tokenInterceptor)

    @UnauthorizedOkHttpClient
    @ViewModelScoped
    @Provides
    fun provideUnauthorizedOkHttpClient(): OkHttpClient.Builder = 
        OkHttpClient.Builder()

    @AuthorizedApiConfig
    @ViewModelScoped
    @Provides
    fun provideApiConfig(
        @AuthorizedOkHttpClient
        okHttpClient: OkHttpClient.Builder,
        @ApplicationContext context: Context
    ): ApiConfig = ApiConfig.getInstance(okHttpClient, context)

    @UnauthorizedApiConfig
    @ViewModelScoped
    @Provides
    fun provideUnauthorizedApiConfig(
        @UnauthorizedOkHttpClient
        okHttpClient: OkHttpClient.Builder,
        @ApplicationContext context: Context
    ): ApiConfig = ApiConfig.getInstance(okHttpClient, context)


    @ViewModelScoped
    @Provides
    fun provideAuthService(
        @UnauthorizedApiConfig
        apiConfig: ApiConfig
    ): AuthService = apiConfig.createService(AuthService::class.java)

    @ViewModelScoped
    @Provides
    fun provideTPSService(
        @AuthorizedApiConfig
        apiConfig: ApiConfig
    ): TPSService = apiConfig.createService(TPSService::class.java)

    @ViewModelScoped
    @Provides
    fun provideElectionService(
        @AuthorizedApiConfig
        apiConfig: ApiConfig
    ): ElectionService = apiConfig.createService(ElectionService::class.java)

    @ViewModelScoped
    @Provides
    fun provideUploadEvidenceService(
        @AuthorizedApiConfig
        apiConfig: ApiConfig
    ): UploadEvidenceService = apiConfig.createService(UploadEvidenceService::class.java)

    @ViewModelScoped
    @Provides
    fun provideVoteService(
        @AuthorizedApiConfig
        apiConfig: ApiConfig
    ): VoteService = apiConfig.createService(VoteService::class.java)
    
}
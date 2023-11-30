package com.haltec.quickcount.di

import com.haltec.quickcount.data.remote.base.ApiConfig
import com.haltec.quickcount.data.remote.service.OfflineService
import com.haltec.quickcount.data.repository.OfflineRepository
import com.haltec.quickcount.data.repository.UploadEvidenceRepository
import com.haltec.quickcount.data.repository.VoteRepository
import com.haltec.quickcount.domain.repository.IOfflineRepository
import com.haltec.quickcount.domain.repository.IUploadEvidenceRepository
import com.haltec.quickcount.domain.repository.IVoteRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Unscoped

@Module
@InstallIn(SingletonComponent::class)
interface IWorkerModule {
    @Unscoped
    @Binds
    fun provideIVoteRepository(repository: VoteRepository): IVoteRepository
    
    @Unscoped
    @Binds
    fun provideIUploadEvidenceRepository(repository: UploadEvidenceRepository): IUploadEvidenceRepository
    
    @Binds
    fun provideIOfflineRepository(repository: OfflineRepository): IOfflineRepository
}

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    
    @Provides
    fun provideOfflineService(
        @AuthorizedApiConfigUnscoped
        apiConfig: ApiConfig
    ): OfflineService = apiConfig.createService(OfflineService::class.java)
}
package com.haltec.quickcount.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import com.haltec.quickcount.data.repository.AuthRepository
import com.haltec.quickcount.data.repository.ElectionRepository
import com.haltec.quickcount.data.repository.TPSRepository
import com.haltec.quickcount.data.repository.UploadEvidenceRepository
import com.haltec.quickcount.data.repository.VoteRepository
import com.haltec.quickcount.domain.repository.IAuthRepository
import com.haltec.quickcount.domain.repository.IElectionRepository
import com.haltec.quickcount.domain.repository.ITPSRepository
import com.haltec.quickcount.domain.repository.IUploadEvidenceRepository
import com.haltec.quickcount.domain.repository.IVoteRepository

@Module
@InstallIn(ViewModelComponent::class)
interface RepositoryModule {
    @Binds
    @ViewModelScoped
    fun provideLoginRepository(repository: AuthRepository): IAuthRepository
    
    @Binds
    @ViewModelScoped
    fun provideTPSRepository(repository: TPSRepository): ITPSRepository

    @Binds
    @ViewModelScoped
    fun provideElectionRepository(repository: ElectionRepository): IElectionRepository

    @Binds
    @ViewModelScoped
    fun provideIUploadEvidenceRepository(repository: UploadEvidenceRepository): IUploadEvidenceRepository

    @Binds
    @ViewModelScoped
    fun provideIVoteRepository(repository: VoteRepository): IVoteRepository
}
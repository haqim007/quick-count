package com.haltec.quickcount.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import com.haltec.quickcount.data.repository.AuthRepository
import com.haltec.quickcount.data.repository.ElectionRepository
import com.haltec.quickcount.data.repository.TPSRepository
import com.haltec.quickcount.domain.repository.IAuthRepository
import com.haltec.quickcount.domain.repository.IElectionRepository
import com.haltec.quickcount.domain.repository.ITPSRepository

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
}
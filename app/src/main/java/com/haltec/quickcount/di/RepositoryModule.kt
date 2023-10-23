package com.haltec.quickcount.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import com.haltec.quickcount.data.repository.LoginRepository
import com.haltec.quickcount.data.repository.TPSRepository
import com.haltec.quickcount.domain.repository.ILoginRepository
import com.haltec.quickcount.domain.repository.ITPSRepository

@Module
@InstallIn(ViewModelComponent::class)
interface RepositoryModule {
    @Binds
    @ViewModelScoped
    fun provideLoginRepository(repository: LoginRepository): ILoginRepository
    
    @Binds
    @ViewModelScoped
    fun provideTPSRepository(repository: TPSRepository): ITPSRepository
}
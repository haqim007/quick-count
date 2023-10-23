package com.haltec.quickcount.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherIO

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @DispatcherIO
    @Provides
    fun provideCoroutineDispatcherIO() = Dispatchers.IO
}
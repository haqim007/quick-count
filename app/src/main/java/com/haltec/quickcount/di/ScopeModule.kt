package com.haltec.quickcount.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object ScopeModule {
    @ApplicationScope
    @Provides
    fun provideApplicationScope(
        @DispatcherIO
        dispatcherIO: CoroutineDispatcher
    ) = CoroutineScope(SupervisorJob() + dispatcherIO)
}

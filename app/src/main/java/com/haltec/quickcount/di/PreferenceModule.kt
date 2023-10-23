package com.haltec.quickcount.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DevicePreference
val Context.deviceDataStore by preferencesDataStore(name="device preference")

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserPreference
val Context.userDataStore by preferencesDataStore(name="user preference")

@Module
@InstallIn(SingletonComponent::class)
object PreferenceModule {
    @DevicePreference
    @Provides
    @Singleton
    fun provideDeviceDataStore(@ApplicationContext context: Context): DataStore<Preferences>{
        return context.deviceDataStore
    }

    @UserPreference
    @Provides
    @Singleton
    fun provideUserDataStore(@ApplicationContext context: Context): DataStore<Preferences>{
        return context.userDataStore
    }
}

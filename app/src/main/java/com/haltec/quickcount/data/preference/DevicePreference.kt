package com.haltec.quickcount.data.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.haltec.quickcount.di.DevicePreference
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevicePreference @Inject constructor(
    @DevicePreference
    private val dataStore: DataStore<Preferences>
){
    suspend fun storeDeviceToken(key: String){
        dataStore.edit {preferences -> 
            preferences[DEVICE_TOKEN] = key
        }
    }
    
    fun getDeviceToken() = dataStore.data.map { preferences -> 
        preferences[DEVICE_TOKEN]
    }
    
    companion object{
        private val DEVICE_TOKEN = stringPreferencesKey("device_token")
    }
}
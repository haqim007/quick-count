package com.haltec.quickcount.data.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.haltec.quickcount.di.DevicePreference
import kotlinx.coroutines.flow.first
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

    suspend fun setOnline(isOnline: Boolean){
        dataStore.edit {preferences ->
            preferences[IS_ONLINE] = isOnline
        }
    }

    suspend fun setHasSync(hasSync: Boolean){
        dataStore.edit {preferences ->
            preferences[HAS_SYNC_FOR_FIRST_TIME] = hasSync
        }
    }

    // only for first time sync a.k.a when hasSync false. after that, makes no effect
    suspend fun setSyncInProgress(inProgress: Boolean){
        if (!getSyncStatus().first().hasSync){
            dataStore.edit {preferences ->
                preferences[SYNC_IN_PROGRESS] = inProgress
            }
        }
    }

    fun isOnline() = dataStore.data.map { preferences ->
        preferences[IS_ONLINE] ?: false
    }
    
    fun getDeviceToken() = dataStore.data.map { preferences -> 
        preferences[DEVICE_TOKEN]
    }

    fun getSyncStatus() = dataStore.data.map { preferences ->
        
        SyncStatus(
            hasSync = preferences[HAS_SYNC_FOR_FIRST_TIME] ?: false,
            syncInProgress = preferences[SYNC_IN_PROGRESS] ?: false,
        )
    }
    
    suspend fun reset(){
        dataStore.edit {preferences ->
            preferences[SYNC_IN_PROGRESS] = false
            preferences[HAS_SYNC_FOR_FIRST_TIME] = false
        }
    }
    
    companion object{
        private val DEVICE_TOKEN = stringPreferencesKey("device_token")
        private val IS_ONLINE = booleanPreferencesKey("is_online")
        private val HAS_SYNC_FOR_FIRST_TIME = booleanPreferencesKey("has_sync_for_first_time")
        private val SYNC_IN_PROGRESS = booleanPreferencesKey("sync_in_progress")
    }
}

data class SyncStatus(
    val hasSync: Boolean,
    val syncInProgress: Boolean
)
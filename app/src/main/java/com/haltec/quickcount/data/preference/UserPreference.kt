package com.haltec.quickcount.data.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.haltec.quickcount.di.UserPreference
import com.haltec.quickcount.domain.model.UserInfo
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreference @Inject constructor(
    @UserPreference
    private val dataStore: DataStore<Preferences>
){
    suspend fun storeUserInfo(userInfo: UserInfo){
        dataStore.edit {preferences ->
            preferences[USER_NAME] = userInfo.name
            userInfo.token?.let {
                preferences[TOKEN] = it
            }
            userInfo.expiredTimestamp?.let {
                preferences[TOKEN_EXPIRE] = it
            }
            preferences[HAS_LOGOUT] = false
        }
    }
    
    suspend fun resetUserInfo(logout: Boolean = false){
        dataStore.edit {preferences ->
            preferences[USER_NAME] = ""
            preferences[TOKEN] = ""
            preferences[TOKEN_EXPIRE] = 0
            preferences[HAS_LOGOUT] = logout
        }
    }
    
    fun getUserInfo() = dataStore.data.map { preferences -> 
        UserInfo(
            name = preferences[USER_NAME] ?: "",
            token = preferences[TOKEN],
            expiredTimestamp = preferences[TOKEN_EXPIRE]
        )
    }
    
    fun getUserName() = dataStore.data.map {preferences ->
        preferences[USER_NAME] ?: ""
    }
    
    companion object{
        private val USER_NAME = stringPreferencesKey("user_name")
        private val TOKEN = stringPreferencesKey("token")
        private val TOKEN_EXPIRE = longPreferencesKey ("token_expire")
        // Flag that the session end because of user logout
        private val HAS_LOGOUT = booleanPreferencesKey("has_logout")
    }
}
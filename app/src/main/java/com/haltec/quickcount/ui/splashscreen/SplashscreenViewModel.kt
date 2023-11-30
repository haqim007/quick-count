package com.haltec.quickcount.ui.splashscreen

import com.haltec.quickcount.data.preference.DevicePreference
import com.haltec.quickcount.data.preference.SyncStatus
import com.haltec.quickcount.data.preference.UserPreference
import com.haltec.quickcount.domain.model.SessionValidity
import com.haltec.quickcount.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class SplashscreenViewModel @Inject constructor(
    devicePreference: DevicePreference,
    userPreference: UserPreference
): BaseViewModel<SplashscreenUiState>() {
    override val _state = MutableStateFlow(SplashscreenUiState())
    
    init {
        devicePreference.getSyncStatus().launchCollectLatest { 
            _state.update { state -> 
                state.copy(syncStatus = it) 
            }
        }
        userPreference.getSessionValidity().launchCollectLatest {
            _state.update { state ->
                state.copy(sessionValid = it)
            }
        }
    }
}

data class SplashscreenUiState(
    val syncStatus: SyncStatus? = null,
    val sessionValid: SessionValidity? = null
)



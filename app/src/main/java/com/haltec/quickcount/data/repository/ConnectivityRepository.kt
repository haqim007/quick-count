package com.haltec.quickcount.data.repository

import com.haltec.quickcount.data.preference.DevicePreference
import com.haltec.quickcount.domain.repository.IConnectivityRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ConnectivityRepository @Inject constructor(
    private val devicePreference: DevicePreference
): IConnectivityRepository {
    override suspend fun setOnline(isOnline: Boolean) {
        devicePreference.setOnline(isOnline)
    }

    override suspend fun isOnline(): Boolean {
        return devicePreference.isOnline().first() ?: false
    }
}
package com.haltec.quickcount.domain.repository

interface IConnectivityRepository {
    suspend fun setOnline(isOnline: Boolean = true)
    suspend fun isOnline(): Boolean
}
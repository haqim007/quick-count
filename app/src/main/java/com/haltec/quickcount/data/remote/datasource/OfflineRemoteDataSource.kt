package com.haltec.quickcount.data.remote.datasource

import dagger.hilt.android.scopes.ViewModelScoped
import com.haltec.quickcount.data.mechanism.getResult
import com.haltec.quickcount.data.remote.service.OfflineService
import com.haltec.quickcount.data.remote.service.TPSService
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineRemoteDataSource @Inject constructor(
    private val offlineService: OfflineService
){
    suspend fun getAllData() = getResult {
        offlineService.getAllData()
    }

}
package com.haltec.quickcount.data.remote.datasource

import com.haltec.quickcount.data.mechanism.getResult
import com.haltec.quickcount.data.remote.service.TPSService
import dagger.hilt.android.scopes.ViewModelScoped
import retrofit2.http.Path
import javax.inject.Inject

@ViewModelScoped
class TPSElectionRemoteDataSource @Inject constructor(
    private val tpsService: TPSService
){

    suspend fun getTPSElectionList(filter: String) = getResult {
        tpsService.getTPSElectionList(filter)
    }
}
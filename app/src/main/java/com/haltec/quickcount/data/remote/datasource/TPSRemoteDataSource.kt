package com.haltec.quickcount.data.remote.datasource

import dagger.hilt.android.scopes.ViewModelScoped
import com.haltec.quickcount.data.mechanism.getResult
import com.haltec.quickcount.data.remote.service.TPSService
import retrofit2.http.Path
import javax.inject.Inject

@ViewModelScoped
class TPSRemoteDataSource @Inject constructor(
    private val tpsService: TPSService
){
    suspend fun getTPSList() = getResult { 
        tpsService.getTPSList()
    }

    suspend fun getTPS(@Path("id") tpsId: Int) = getResult { 
        tpsService.getTPS(tpsId)
    }
}
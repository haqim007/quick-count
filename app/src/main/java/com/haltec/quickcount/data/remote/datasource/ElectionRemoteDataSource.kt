package com.haltec.quickcount.data.remote.datasource

import com.haltec.quickcount.data.mechanism.getResult
import com.haltec.quickcount.data.remote.service.ElectionService
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class ElectionRemoteDataSource @Inject constructor(
    private val service: ElectionService
) {
    suspend fun getElectionList(tpsId: Int) = getResult { 
        service.getElectionList(tpsId)
    }
}
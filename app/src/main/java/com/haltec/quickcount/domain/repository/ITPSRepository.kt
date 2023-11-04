package com.haltec.quickcount.domain.repository

import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.ElectionFilter
import com.haltec.quickcount.domain.model.TPS
import com.haltec.quickcount.domain.model.TPSElection
import kotlinx.coroutines.flow.Flow

interface ITPSRepository {
    suspend fun getUsername(): String
    fun getTPSList(): Flow<Resource<List<TPS>>>
    fun getTPS(tpsId: Int): Flow<Resource<TPS>>
    
    fun getTPSElections(filter: String = ""): Flow<Resource<List<TPSElection>>>
}
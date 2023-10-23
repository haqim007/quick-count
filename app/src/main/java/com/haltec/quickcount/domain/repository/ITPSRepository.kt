package com.haltec.quickcount.domain.repository

import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.TPS
import kotlinx.coroutines.flow.Flow

interface ITPSRepository {
    suspend fun getUsername(): String
    fun getTPSList(): Flow<Resource<List<TPS>>>
    fun getTPS(tpsId: Int): Flow<Resource<TPS>>
}
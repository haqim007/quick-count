package com.haltec.quickcount.domain.repository

import androidx.paging.PagingData
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.TPS
import kotlinx.coroutines.flow.Flow

interface ITPSRepository {
    suspend fun getUsername(): String
    fun getTPSList(): Flow<PagingData<TPS>>
    fun getTPS(tpsId: Int): Flow<Resource<TPS>>
    
    suspend fun countTPS(): Int
    
}
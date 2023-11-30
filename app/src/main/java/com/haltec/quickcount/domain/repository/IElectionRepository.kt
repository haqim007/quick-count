package com.haltec.quickcount.domain.repository

import androidx.paging.PagingData
import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.Election
import kotlinx.coroutines.flow.Flow

interface IElectionRepository {
    fun getElectionList(tpsId: Int): Flow<PagingData<Election>>
}
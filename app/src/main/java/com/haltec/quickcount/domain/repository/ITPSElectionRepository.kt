package com.haltec.quickcount.domain.repository

import androidx.paging.PagingData
import com.haltec.quickcount.domain.model.ElectionFilter
import com.haltec.quickcount.domain.model.TPSElection
import kotlinx.coroutines.flow.Flow

interface ITPSElectionRepository {
    fun getTPSElections(filter: ElectionFilter): Flow<PagingData<TPSElection>>
}
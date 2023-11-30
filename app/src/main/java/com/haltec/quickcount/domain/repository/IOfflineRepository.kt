package com.haltec.quickcount.domain.repository

import com.haltec.quickcount.data.mechanism.Resource
import com.haltec.quickcount.domain.model.BasicMessage
import kotlinx.coroutines.flow.Flow

interface IOfflineRepository {
    fun getAllData(): Flow<Resource<BasicMessage>>
}
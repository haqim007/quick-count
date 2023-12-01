package com.haltec.quickcount.data.local.dataSource

import androidx.room.withTransaction
import com.haltec.quickcount.data.local.entity.table.TempUploadEvidenceEntity
import com.haltec.quickcount.data.local.entity.table.UploadedEvidenceEntity
import com.haltec.quickcount.data.local.room.AppDatabase
import javax.inject.Inject

/**
 * Vote local data source
 *
 * @property database
 * @constructor 
 */

class UploadEvidenceLocalDataSource @Inject constructor(
    private val database: AppDatabase
) {
    suspend fun removeTempUploadEvidence(tpsId: Int, electionId: Int, type: String){
        database.uploadEvidenceDao().removeTempUploadEvidence(tpsId, electionId, type)
    }

    suspend fun insertOrReplaceUploadEvidence(
        uploadedEvidence: UploadedEvidenceEntity,
        tempUploadEvidence: TempUploadEvidenceEntity
    ){
        database.uploadEvidenceDao().insertOrReplaceUploadEvidence(uploadedEvidence, tempUploadEvidence)
    }

    suspend fun getTempUploadEvidence(): List<TempUploadEvidenceEntity> {
        return database.uploadEvidenceDao().getTempUploadEvidence()
    }

    suspend fun insertUploadedEvidence(
        uploadedEvidence: List<UploadedEvidenceEntity>
    ){
        database.uploadEvidenceDao().replaceUploadedEvidence(uploadedEvidence)
    }

    suspend fun insertUploadedEvidence(
        uploadedEvidence: List<UploadedEvidenceEntity>,
        tpsIds: List<Int>,
        electionIds: List<Int>
    ){
        database.withTransaction {
            database.uploadEvidenceDao().clearAllByTpsElections(tpsIds, electionIds)
            database.uploadEvidenceDao().replaceUploadedEvidence(uploadedEvidence)
        }
    }
    suspend fun getUploadedEvidence(tpsId: Int, electionId: Int): List<UploadedEvidenceEntity> {
        return database.uploadEvidenceDao().getUploadedEvidence(tpsId, electionId)
    }

}
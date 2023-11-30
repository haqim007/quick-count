package com.haltec.quickcount.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.haltec.quickcount.data.local.entity.table.TEMP_UPLOAD_EVIDENCE_TABLE
import com.haltec.quickcount.data.local.entity.table.TempUploadEvidenceEntity
import com.haltec.quickcount.data.local.entity.table.UPLOADED_EVIDENCE_TABLE
import com.haltec.quickcount.data.local.entity.table.UploadedEvidenceEntity
import com.haltec.quickcount.data.local.entity.table.VOTE_FORM_TABLE
import kotlin.reflect.typeOf

@Dao
interface UploadEvidenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUploadEvidence(uploadedEvidence: UploadedEvidenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUploadEvidence(uploadedEvidence: List<UploadedEvidenceEntity>)

    @Query("SELECT * FROM $UPLOADED_EVIDENCE_TABLE")
    suspend fun getUploadedEvidence(): List<UploadedEvidenceEntity>

    @Query("SELECT * FROM $UPLOADED_EVIDENCE_TABLE WHERE tps_id = :tpsId AND election_id = :electionId")
    suspend fun getUploadedEvidence(tpsId: Int, electionId: Int): List<UploadedEvidenceEntity>

    @Query("SELECT * FROM $UPLOADED_EVIDENCE_TABLE WHERE tps_id = :tpsId AND election_id = :electionId AND type = :type")
    suspend fun getUploadedEvidence(tpsId: Int, electionId: Int, type: String): UploadedEvidenceEntity?

    @Transaction
    suspend fun insertOrReplaceUploadedEvidence(uploadedEvidence: UploadedEvidenceEntity){
        val existing = getUploadedEvidence(
            tpsId = uploadedEvidence.tpsId, 
            electionId = uploadedEvidence.electionId, 
            type = uploadedEvidence.type
        )
        existing?.let { 
            insertUploadEvidence(
                existing.copy(
                    longitude = uploadedEvidence.longitude,
                    latitude = uploadedEvidence.latitude,
                    file = uploadedEvidence.file,
                    fileUrl = uploadedEvidence.fileUrl
                )
            )
        } ?: run{
            insertUploadEvidence(uploadedEvidence)
        }
    }

    @Query("DELETE FROM $UPLOADED_EVIDENCE_TABLE WHERE tps_id = :tpsId AND election_id = :electionId AND type = :type")
    suspend fun removeUploadedEvidence(tpsId: Int, electionId: Int, type: String)

    @Transaction
    suspend fun replaceUploadedEvidence(uploadedEvidence: UploadedEvidenceEntity){
        val existing = getUploadedEvidence(
            tpsId = uploadedEvidence.tpsId,
            electionId = uploadedEvidence.electionId,
            type = uploadedEvidence.type
        )
        existing?.let {
            removeUploadedEvidence(tpsId = existing.tpsId, electionId = existing.electionId, type = existing.type)
        }
        insertUploadEvidence(uploadedEvidence)
    }

    @Transaction
    suspend fun replaceUploadedEvidence(uploadedEvidence: List<UploadedEvidenceEntity>){
        uploadedEvidence.forEach {
            val existing = getUploadedEvidence(
                tpsId = it.tpsId,
                electionId = it.electionId,
                type = it.type
            )
            existing?.let {
                removeUploadedEvidence(tpsId = existing.tpsId, electionId = existing.electionId, type = existing.type)
            }
            insertUploadEvidence(uploadedEvidence)
        }
    }
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTempUploadEvidence(tempUploadEvidence: TempUploadEvidenceEntity)

    @Query("DELETE FROM $TEMP_UPLOAD_EVIDENCE_TABLE WHERE tps_id = :tpsId AND election_id = :electionId AND type = :type")
    suspend fun removeTempUploadEvidence(tpsId: Int, electionId: Int, type: String)
    
    @Transaction
    suspend fun insertOrReplaceTempUploadEvidence(tempUploadEvidence: TempUploadEvidenceEntity){
        removeTempUploadEvidence(tempUploadEvidence.tpsId, tempUploadEvidence.electionId, tempUploadEvidence.type)
        insertTempUploadEvidence(tempUploadEvidence)
    }

    @Query("SELECT * FROM $TEMP_UPLOAD_EVIDENCE_TABLE")
    suspend fun getTempUploadEvidence(): List<TempUploadEvidenceEntity>
    
    @Transaction
    suspend fun insertOrReplaceUploadEvidence(uploadedEvidence: UploadedEvidenceEntity, tempUploadEvidence: TempUploadEvidenceEntity){
        insertOrReplaceTempUploadEvidence(tempUploadEvidence)
        insertOrReplaceUploadedEvidence(uploadedEvidence)
    }

    @Query("DELETE FROM $UPLOADED_EVIDENCE_TABLE WHERE tps_id NOT IN (:tpsIds)")
    suspend fun clearAllByTps(tpsIds: List<Int>)

}
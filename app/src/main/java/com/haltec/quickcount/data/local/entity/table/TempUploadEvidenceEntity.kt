package com.haltec.quickcount.data.local.entity.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File


const val TEMP_UPLOAD_EVIDENCE_TABLE = "temp_upload_evidence"

@Entity(TEMP_UPLOAD_EVIDENCE_TABLE)
data class TempUploadEvidenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo("tps_id")
    val tpsId: Int,
    @ColumnInfo("election_id")
    val electionId: Int,
    val type: String,
    val description: String,
    val longitude: String,
    val latitude: String,
    val file: File
)
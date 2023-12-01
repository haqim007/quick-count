package com.haltec.quickcount.data.local.entity.table

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.haltec.quickcount.domain.model.VoteEvidence
import com.haltec.quickcount.util.stringToStringDateID
import java.io.File


const val UPLOADED_EVIDENCE_TABLE = "uploaded_evidence"

@Entity(
    tableName = UPLOADED_EVIDENCE_TABLE,
)
data class UploadedEvidenceEntity(
    @PrimaryKey()
    val id: Int,
    @ColumnInfo("tps_id")
    val tpsId: Int,
    @ColumnInfo("election_id")
    val electionId: Int,
    val type: String,
    val description: String,
    val longitude: String,
    val latitude: String,
    val file: File?,
    @ColumnInfo("file_url")
    val fileUrl: String?,
    @ColumnInfo("uploaded_at")
    val uploadedAt: String
){
    fun toModel() = VoteEvidence(
        this.latitude,
        this.description,
        "",
        this.type,
        "",
        this.tpsId,
        this.fileUrl,
        "",
        stringToStringDateID(this.uploadedAt),
        this.electionId,
        "",
        this.id,
        this.longitude,
        file
    )
}

fun List<UploadedEvidenceEntity>.toModel() = this.map { 
    it.toModel()
}
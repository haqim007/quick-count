package com.haltec.quickcount.domain.model

import java.io.File

data class VoteEvidence(
    val latitude: String,
    val description: String,
    val createdAt: String,
    val type: String,
    val createdBy: String,
    val tpsId: Int,
    val fileUrl: String?,
    val updatedAt: String,
    val uploadedAt: String,
    val selectionTypeId: Int,
    val updatedBy: String,
    val id: Int,
    val longitude: String,
    val file: File? = null
)

package com.haltec.quickcount.domain.model

data class VoteEvidence(
    val latitude: String,
    val description: String,
    val createdAt: String,
    val type: String,
    val createdBy: String,
    val tpsId: Int,
    val file: String,
    val updatedAt: String,
    val uploadedAt: String,
    val selectionTypeId: Int,
    val updatedBy: String,
    val id: Int,
    val longitude: String
)

package com.haltec.quickcount.data.local.entity.view

import com.haltec.quickcount.domain.model.TPSElection
import com.haltec.quickcount.domain.model.stringNumberToSubmitVoteStatus
import com.haltec.quickcount.util.stringToStringDateID

const val TPS_ELECTION_VIEW = "TPS_ELECTION_VIEW"
data class TPSElectionEntity(
    // from tps entity
    val tpsId: Int,
    val tpsName: String,
    val villageCode: String,
    val address: String,
    val city: String,
    val longitude: String,
    val latitude: String,
    val dpt: Int,
    val createdAt: String,
    val createdBy: String,
    val subdistrict: String,
    val province: String,
    val village: String,
    
    // from election entity
    val electionId: Int,
    val electionName: String,
    val statusVote: String,
    
    val lastVoteUpdated: String
){
    fun toModel() = TPSElection(
        villageCode, address, city, latitude, dpt, createdAt, createdBy, 
        electionName, subdistrict, province, electionId, tpsName, tpsId, 
        village, longitude, stringNumberToSubmitVoteStatus(statusVote), stringToStringDateID(lastVoteUpdated) 
    )
}
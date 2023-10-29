package com.haltec.quickcount.domain.model

data class VoteData(
    val tpsId: Int,
    val province: String,
    val subdistrict: String,
    val partyLists: List<PartyListsItem>,
    val village: String,
    val city: String,
    val tpsName: String,

){
    data class PartyListsItem(
        val candidateList: List<Candidate>,
        val partyName: String,
        val id: Int,
        val isExpanded: Boolean = false,
        val totalPartyVote: Int = 0,
        val totalVote: Int = 0,
        val requestFocus: Boolean = false
    )

    data class Candidate(
        val orderNumber: Int,
        val candidateName: String,
        val id: Int,
        val totalCandidateVote: Int = 0,
        val requestFocus: Boolean = false
    )
}



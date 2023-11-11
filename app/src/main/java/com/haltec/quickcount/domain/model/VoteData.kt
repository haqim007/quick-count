package com.haltec.quickcount.domain.model

/**
 * Vote data
 *
 * @property tpsId
 * @property province
 * @property subdistrict
 * @property partyLists
 * @property village
 * @property city
 * @property tpsName
 * @property validVote total valid vote
 * @property invalidVote total invalid vote
 * @property isParty Include party vote, when true it means, there will not be input for party vote. only candidate vote
 *
 * @constructor Create empty Vote data
 */
data class VoteData(
    val tpsId: Int,
    val province: String,
    val subdistrict: String,
    val partyLists: List<PartyListsItem>,
    val village: String,
    val city: String,
    val tpsName: String,
    val validVote: Int,
    val invalidVote: Int,
    val note: String?,
    val isParty: Boolean,
){
    /**
     * Party lists item
     *
     * @property candidateList
     * @property partyName
     * @property id
     * @property isExpanded
     * @property totalPartyVote
     * @property totalVote totalPartiVote + sum of totalCandidateVote of [Candidate]
     * @constructor Create empty Party lists item
     */
    data class PartyListsItem(
        val candidateList: List<Candidate>,
        val partyName: String,
        val id: Int,
        val isExpanded: Boolean = false,
        val totalPartyVote: Int = 0,
        val totalVote: Int = 0,
    ){
        /**
         * Include party vote, when true it means, there will not be input for party vote. 
         * only candidate vote
         */
        val includePartyVote: Boolean
            get() = id != 0
    }

    /**
     * Candidate
     *
     * @property orderNumber
     * @property candidateName
     * @property id
     * @property totalCandidateVote
     * @constructor Create empty Candidate
     */
    data class Candidate(
        val orderNumber: Int,
        val candidateName: String,
        val id: Int,
        val totalCandidateVote: Int = 0,
    )
}



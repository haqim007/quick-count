package com.haltec.quickcount.domain.model

/**
 * Election status
 *
 * @property label
 * @property valueText
 * @property valueNumber
 * @constructor Create empty Election status
 */
enum class SubmitVoteStatus (val label: String, val valueText: String, val valueNumber: String){
    /**
     * Submitted
     *
     * @constructor Create empty Submitted
     */
    SUBMITTED("Belum Terverifikasi", "submit", "0"),

    /**
     * Pending
     *
     * @constructor Create empty Pending
     */
    PENDING("Belum Dikirim", "pending", ""),

    /**
     * Pending
     *
     * @constructor Create empty Pending
     */
    IN_QUEUE("Menunggu Dikirim", "in_queue", "-2"),

    /**
     * Verified
     *
     * @constructor Create empty Verified
     */
    VERIFIED("Sudah Terverifikasi", "approve", "1"),

    /**
     * Rejected
     *
     * @constructor Create empty Rejected
     */
    REJECTED("Ditolak", "reject", "-1")
}

fun stringToSubmitVoteStatusValueText(value: String): String {
    return when (value) {
        SubmitVoteStatus.PENDING.label -> SubmitVoteStatus.PENDING.valueText
        SubmitVoteStatus.IN_QUEUE.label -> SubmitVoteStatus.IN_QUEUE.valueText
        SubmitVoteStatus.SUBMITTED.label -> SubmitVoteStatus.SUBMITTED.valueText
        SubmitVoteStatus.VERIFIED.label -> SubmitVoteStatus.VERIFIED.valueText
        SubmitVoteStatus.REJECTED.label -> SubmitVoteStatus.REJECTED.valueText
        else -> ""
    }
}

fun stringToSubmitVoteStatusNumber(value: String): String? {
    return when (value) {
        SubmitVoteStatus.PENDING.label -> SubmitVoteStatus.PENDING.valueNumber
        SubmitVoteStatus.IN_QUEUE.label -> SubmitVoteStatus.IN_QUEUE.valueNumber
        SubmitVoteStatus.SUBMITTED.label -> SubmitVoteStatus.SUBMITTED.valueNumber
        SubmitVoteStatus.VERIFIED.label -> SubmitVoteStatus.VERIFIED.valueNumber
        SubmitVoteStatus.REJECTED.label -> SubmitVoteStatus.REJECTED.valueNumber
        else -> null
    }
}


fun stringToSubmitVoteStatus(status: String): SubmitVoteStatus{
    return when(status){
        SubmitVoteStatus.SUBMITTED.valueText -> SubmitVoteStatus.SUBMITTED
        SubmitVoteStatus.VERIFIED.valueText -> SubmitVoteStatus.VERIFIED
        SubmitVoteStatus.REJECTED.valueText -> SubmitVoteStatus.REJECTED
        SubmitVoteStatus.PENDING.valueText -> SubmitVoteStatus.PENDING
        SubmitVoteStatus.IN_QUEUE.valueText -> SubmitVoteStatus.IN_QUEUE
        else -> SubmitVoteStatus.PENDING
    }
}

fun submitVoteStatusValueTextToValueNumber(status: String): String{
    return when(status){
        SubmitVoteStatus.SUBMITTED.valueText -> SubmitVoteStatus.SUBMITTED.valueNumber
        SubmitVoteStatus.VERIFIED.valueText -> SubmitVoteStatus.VERIFIED.valueNumber
        SubmitVoteStatus.REJECTED.valueText -> SubmitVoteStatus.REJECTED.valueNumber
        SubmitVoteStatus.PENDING.valueText -> SubmitVoteStatus.PENDING.valueNumber
        SubmitVoteStatus.IN_QUEUE.valueText -> SubmitVoteStatus.IN_QUEUE.valueNumber
        else -> SubmitVoteStatus.PENDING.valueNumber
    }
}

fun stringNumberToSubmitVoteStatus(status: String): SubmitVoteStatus{
    return when(status){
        SubmitVoteStatus.SUBMITTED.valueNumber -> SubmitVoteStatus.SUBMITTED
        SubmitVoteStatus.VERIFIED.valueNumber -> SubmitVoteStatus.VERIFIED
        SubmitVoteStatus.REJECTED.valueNumber -> SubmitVoteStatus.REJECTED
        SubmitVoteStatus.PENDING.valueNumber -> SubmitVoteStatus.PENDING
        SubmitVoteStatus.IN_QUEUE.valueNumber -> SubmitVoteStatus.IN_QUEUE
        else -> SubmitVoteStatus.PENDING
    }
}
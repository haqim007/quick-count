package com.haltec.quickcount.domain.model

/**
 * Election status
 *
 * @property text
 * @property valueText
 * @property valueNumber
 * @constructor Create empty Election status
 */
enum class SubmitVoteStatus (val text: String, val valueText: String, val valueNumber: String){
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
        SubmitVoteStatus.PENDING.text -> SubmitVoteStatus.PENDING.valueText
        SubmitVoteStatus.IN_QUEUE.text -> SubmitVoteStatus.IN_QUEUE.valueText
        SubmitVoteStatus.SUBMITTED.text -> SubmitVoteStatus.SUBMITTED.valueText
        SubmitVoteStatus.VERIFIED.text -> SubmitVoteStatus.VERIFIED.valueText
        SubmitVoteStatus.REJECTED.text -> SubmitVoteStatus.REJECTED.valueText
        else -> ""
    }
}

fun stringToSubmitVoteStatusNumber(value: String): String? {
    return when (value) {
        SubmitVoteStatus.PENDING.text -> SubmitVoteStatus.PENDING.valueNumber
        SubmitVoteStatus.IN_QUEUE.text -> SubmitVoteStatus.IN_QUEUE.valueNumber
        SubmitVoteStatus.SUBMITTED.text -> SubmitVoteStatus.SUBMITTED.valueNumber
        SubmitVoteStatus.VERIFIED.text -> SubmitVoteStatus.VERIFIED.valueNumber
        SubmitVoteStatus.REJECTED.text -> SubmitVoteStatus.REJECTED.valueNumber
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
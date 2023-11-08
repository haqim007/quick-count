package com.haltec.quickcount.domain.model

/**
 * Election status
 *
 * @property text
 * @property valueText
 * @property valueNumber
 * @constructor Create empty Election status
 */
enum class ElectionStatus (val text: String, val valueText: String, val valueNumber: String){
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
     * Verified
     *
     * @constructor Create empty Verified
     */
    VERIFIED("Sudah Terverifikasi", "approved", "1"),

    /**
     * Rejected
     *
     * @constructor Create empty Rejected
     */
    REJECTED("Ditolak", "rejected", "-1")
}
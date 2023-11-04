package com.haltec.quickcount.domain.model

enum class ElectionStatus (val text: String, val valueText: String, val valueNumber: String){
    SUBMITTED("Belum Terverifikasi", "submit", "0"),
    PENDING("Belum Dikirim", "pending", ""),
    VERIFIED("Sudah Terverifikasi", "approved", "1"),
    REJECTED("Ditolak", "rejected", "-1")
}
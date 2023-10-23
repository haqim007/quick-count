package com.haltec.quickcount.domain.model

data class UserInfo(
    val name: String = "",
    val token: String? = null,
    val expiredTimestamp: Long? = null
)

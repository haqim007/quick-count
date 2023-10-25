package com.haltec.quickcount.domain.model


/**
 * Session validity
 *
 * @property isValid Whether the session is still valid
 * @property hasLogout Whether the session is invalid because of user logout
 * @constructor Create empty Session validity
 */
data class SessionValidity(
    val isValid: Boolean,
    val hasLogout: Boolean
)

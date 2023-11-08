package com.haltec.quickcount.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Tps
 *
 * @property villageCode
 * @property address
 * @property city
 * @property latitude
 * @property pending total pending
 * @property dpt
 * @property createdAt
 * @property createdBy
 * @property approved total approved
 * @property subdistrict
 * @property province
 * @property name
 * @property id
 * @property submitted total submitted
 * @property village
 * @property longitude
 * @property rejected total rejected
 * @constructor Create empty Tps
 */
@Parcelize
data class TPS(
    val villageCode: String,
    val address: String,
    val city: String,
    val latitude: String,
    val pending: Int?,
    val dpt: Int,
    val createdAt: String,
    val createdBy: String,
    val approved: Int?,
    val subdistrict: String,
    val province: String,
    val name: String,
    val id: Int,
    val submitted: Int?,
    val village: String,
    val longitude: String,
    val rejected: Int?
): Parcelable

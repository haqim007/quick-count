package com.haltec.quickcount.data.local.entity.table

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * To store information of fetched latest page
 *
 * @property id
 * @property prevKey
 * @property nextKey
 * @constructor Create empty Remote keys
 */
@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tableId: Int,
    val tableName: String,
    val prevKey: Int?,
    val nextKey: Int?
)
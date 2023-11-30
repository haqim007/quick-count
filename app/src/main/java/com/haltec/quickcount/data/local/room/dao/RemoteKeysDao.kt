package com.haltec.quickcount.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.haltec.quickcount.data.local.entity.table.RemoteKeys

@Dao
interface RemoteKeysDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey:List<RemoteKeys>)

    @Query("SELECT * FROM remote_keys where tableId = :tableId AND tableName = :tableName")
    suspend fun getRemoteKeyById(tableId: Int, tableName: String): RemoteKeys?

    @Query("DELETE FROM remote_keys where tableName = :tableName")
    suspend fun clearRemoteKeys(tableName: String)
}
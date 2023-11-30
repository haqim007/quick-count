package com.haltec.quickcount.data.local.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.haltec.quickcount.data.local.entity.table.RemoteKeys
import com.haltec.quickcount.data.local.entity.table.TPSEntity
import com.haltec.quickcount.data.local.entity.table.TPS_TABLE

@Dao
interface TPSDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tps:List<TPSEntity>)
    
    @Query("SELECT * FROM $TPS_TABLE")
    fun getPaging(): PagingSource<Int, TPSEntity>

    @Query("SELECT * FROM $TPS_TABLE where id = :id")
    suspend fun getById(id: Int): TPSEntity?

    @Query("DELETE FROM $TPS_TABLE")
    suspend fun clearAll()

    @Update
    suspend fun update(tps: TPSEntity)
    
    @Transaction
    suspend fun increaseTotalWaitToBeSentData(tpsId: Int){
        val existing = getById(tpsId)
        existing?.let { 
            update(
                it.copy(
                    waitToBeSent = ((it.waitToBeSent.toIntOrNull() ?: 0) + 1).toString()
                )
            )
        }
    }

    @Transaction
    suspend fun decreaseTotalWaitToBeSentData(tpsId: Int){
        val existing = getById(tpsId)
        existing?.let {
            val currentTotal = (it.waitToBeSent.toIntOrNull() ?: 0)
            if (currentTotal > 0){
                update(
                    it.copy(
                        waitToBeSent = (currentTotal - 1).toString()
                    )
                )
            }
        }
    }
}
package com.haltec.quickcount.data.local.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.haltec.quickcount.data.local.entity.table.ELECTION_TABLE
import com.haltec.quickcount.data.local.entity.table.ElectionEntity
import com.haltec.quickcount.data.local.entity.table.PartyEntity

@Dao
interface ElectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(election: List<ElectionEntity>)

    @Query("SELECT * FROM $ELECTION_TABLE where tps_id = :tpsId")
    suspend fun getByTpsId(tpsId: Int): ElectionEntity?

    @Query("SELECT * FROM $ELECTION_TABLE where id = :id")
    suspend fun getById(id: Int): ElectionEntity?

    @Query("DELETE FROM $ELECTION_TABLE")
    suspend fun clearAll()

    @Query("DELETE FROM $ELECTION_TABLE WHERE tps_id NOT IN (:tpsIds)")
    suspend fun clearAll(tpsIds: List<Int>)

    @Query("SELECT * FROM $ELECTION_TABLE where tps_id = :tpsId")
    fun getPaging(tpsId: Int): PagingSource<Int, ElectionEntity>

    @Query("UPDATE $ELECTION_TABLE SET status_vote = :status WHERE id = :electionId")
    suspend fun updateStatus(electionId: Int, status: String)
}
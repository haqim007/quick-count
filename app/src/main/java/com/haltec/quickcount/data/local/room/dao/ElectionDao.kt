package com.haltec.quickcount.data.local.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.haltec.quickcount.data.local.entity.table.ELECTION_TABLE
import com.haltec.quickcount.data.local.entity.table.ElectionEntity

@Dao
interface ElectionDao {
    @Transaction
    suspend fun insertAll(election: List<ElectionEntity>, isFromTPSElection: Boolean = false){
        election.forEach {
            val existing = get(it.electionId, tpsId = it.tpsId)
            existing?.let { prev -> 
                clearById(prev.id)
                if (isFromTPSElection){
                    insert(
                        it.copy(
                            createdBy = prev.createdBy,
                            createdAt = prev.createdAt,
                            active = prev.active,
                        )
                    )
                }else{
                    insert(it)
                }
            } ?: run{
                insert(it)
            }
            
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(election: ElectionEntity)

    @Query("SELECT * FROM $ELECTION_TABLE where tps_id = :tpsId")
    suspend fun getByTpsId(tpsId: Int): ElectionEntity?

    @Query("SELECT * FROM $ELECTION_TABLE where election_id = :id")
    suspend fun getById(id: Int): ElectionEntity?

    @Query("SELECT * FROM $ELECTION_TABLE where election_id = :id AND tps_id = :tpsId")
    suspend fun get(id: Int, tpsId: Int): ElectionEntity?

    @Query("DELETE FROM $ELECTION_TABLE")
    suspend fun clearAll()

    @Query("DELETE FROM $ELECTION_TABLE WHERE tps_id NOT IN (:tpsIds)")
    suspend fun clearAll(tpsIds: List<Int>)

    @Query("DELETE FROM $ELECTION_TABLE WHERE tps_id = :tpsId")
    suspend fun clearAll(tpsId: Int)

    @Query("DELETE FROM $ELECTION_TABLE WHERE election_id = :id")
    suspend fun clearByElectionId(id: Int)

    @Query("DELETE FROM $ELECTION_TABLE WHERE id = :id")
    suspend fun clearById(id: Int)

    @Query("DELETE FROM $ELECTION_TABLE WHERE status_vote = :filter")
    suspend fun clearByFilter(filter: String)

    @Query("SELECT * FROM $ELECTION_TABLE where tps_id = :tpsId")
    fun getPaging(tpsId: Int): PagingSource<Int, ElectionEntity>

    @Query("UPDATE $ELECTION_TABLE SET status_vote = :status WHERE tps_id = :tpsId AND election_id = :electionId")
    suspend fun updateStatus(tpsId: Int, electionId: Int, status: String)
}
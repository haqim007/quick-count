package com.haltec.quickcount.data.local.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.haltec.quickcount.data.local.entity.table.ELECTION_TABLE
import com.haltec.quickcount.data.local.entity.table.TPS_TABLE
import com.haltec.quickcount.data.local.entity.view.TPSElectionEntity

@Dao
interface TPSElectionDao {
    @Query("""
        SELECT 
            $ELECTION_TABLE.tps_id as tpsId, 
            $TPS_TABLE.name as tpsName,
            village_code as villageCode,
            address,
            city,
            longitude,
            latitude,
            dpt,
            $TPS_TABLE.created_at as createdAt,
            $TPS_TABLE.created_by as createdBy,
            subdistrict,
            province,
            village,
            $ELECTION_TABLE.id as electionId,
            $ELECTION_TABLE.title as electionName,
            status_vote as statusVote
            
        FROM $ELECTION_TABLE
        
        LEFT JOIN $TPS_TABLE ON $TPS_TABLE.id = $ELECTION_TABLE.tps_id
        
        WHERE status_vote = :filter
        
    """)
    fun getPaging(filter: String = ""): PagingSource<Int, TPSElectionEntity>

    @Query("""
        SELECT 
            $ELECTION_TABLE.tps_id as tpsId, 
            $TPS_TABLE.name as tpsName,
            village_code as villageCode,
            address,
            city,
            longitude,
            latitude,
            dpt,
            $TPS_TABLE.created_at as createdAt,
            $TPS_TABLE.created_by as createdBy,
            subdistrict,
            province,
            village,
            $ELECTION_TABLE.id as electionId,
            $ELECTION_TABLE.title as electionName,
            status_vote as statusVote
            
        FROM $ELECTION_TABLE
        
        LEFT JOIN $TPS_TABLE ON $TPS_TABLE.id = $ELECTION_TABLE.tps_id
        
    """)
    fun getPaging(): PagingSource<Int, TPSElectionEntity>
}
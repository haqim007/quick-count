package com.haltec.quickcount.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.haltec.quickcount.data.local.entity.table.ElectionEntity
import com.haltec.quickcount.data.local.entity.table.RemoteKeys
import com.haltec.quickcount.data.local.entity.table.TPSEntity
import com.haltec.quickcount.data.local.entity.table.TempUploadEvidenceEntity
import com.haltec.quickcount.data.local.entity.table.TempVoteSubmitEntity
import com.haltec.quickcount.data.local.entity.table.UploadedEvidenceEntity
import com.haltec.quickcount.data.local.entity.table.VoteFormEntity
import com.haltec.quickcount.data.local.entity.typeConverter.AppTypeConverter
import com.haltec.quickcount.data.local.room.dao.ElectionDao
import com.haltec.quickcount.data.local.room.dao.RemoteKeysDao
import com.haltec.quickcount.data.local.room.dao.TPSDao
import com.haltec.quickcount.data.local.room.dao.TPSElectionDao
import com.haltec.quickcount.data.local.room.dao.UploadEvidenceDao
import com.haltec.quickcount.data.local.room.dao.VoteFormDao

@Database(
    entities = [
        RemoteKeys::class,
        TPSEntity::class,
        ElectionEntity::class,
        VoteFormEntity::class,
        TempVoteSubmitEntity::class,
        TempUploadEvidenceEntity::class,
        UploadedEvidenceEntity::class
    ],
    version = 18,
    exportSchema = false
)
@TypeConverters(AppTypeConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun remoteKeysDao(): RemoteKeysDao
    abstract fun tpsDao(): TPSDao
    abstract fun electionDao(): ElectionDao
    abstract fun voteFormDao(): VoteFormDao
    abstract fun tpsElectionDao(): TPSElectionDao
    
    abstract fun uploadEvidenceDao() : UploadEvidenceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "haltect-quick-count.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { 
                        INSTANCE = it
                    }
            }
        }
    }
}
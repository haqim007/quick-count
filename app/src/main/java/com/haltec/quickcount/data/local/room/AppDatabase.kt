package com.haltec.quickcount.data.local.room

//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import dev.haqim.quickcount.data.local.entity.GameDetailEntity
//import dev.haqim.quickcount.data.local.entity.GamesCollectionEntity
//import dev.haqim.quickcount.data.local.entity.GamesListItemEntity
//import dev.haqim.quickcount.data.local.entity.RemoteKeys
//import dev.haqim.quickcount.data.local.room.dao.GameDetailDao
//import dev.haqim.quickcount.data.local.room.dao.GameCollectionsDao
//import dev.haqim.quickcount.data.local.room.dao.GamesListDao
//import dev.haqim.quickcount.data.local.room.dao.RemoteKeysDao
//
//@Database(
//    entities = [
//        RemoteKeys::class,
//        GamesListItemEntity::class, 
//        GameDetailEntity::class,
//        GamesCollectionEntity::class,
//    ],
//    version = 2,
//    exportSchema = false
//)
//abstract class AppDatabase: RoomDatabase() {
//    abstract fun remoteKeysDao(): RemoteKeysDao
//    abstract fun gamesListDao(): GamesListDao
//    abstract fun gameDetailDao(): GameDetailDao
//    abstract fun gameCollectionDao(): GameCollectionsDao
//    
//    companion object {
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//        
//        @JvmStatic
//        fun getInstance(context: Context): AppDatabase{
//            return INSTANCE ?: synchronized(this){
//                INSTANCE ?: Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    "my_rawg.db"
//                )
//                    .fallbackToDestructiveMigration()
//                    .build()
//                    .also { 
//                        INSTANCE = it
//                    }
//            }
//        }
//    }
//}
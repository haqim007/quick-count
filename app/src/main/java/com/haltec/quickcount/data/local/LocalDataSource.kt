package com.haltec.quickcount.data.local
//
//import androidx.paging.PagingSource
//import androidx.room.withTransaction
//import dev.haqim.quickcount.data.local.entity.GameDetailEntity
//import dev.haqim.quickcount.data.local.entity.GameDetailWithCollectionStatusEntity
//import dev.haqim.quickcount.data.local.entity.GamesCollectionEntity
//import dev.haqim.quickcount.data.local.entity.GamesListItemEntity
//import dev.haqim.quickcount.data.local.entity.RemoteKeys
//import dev.haqim.quickcount.data.local.room.AppDatabase
//import kotlinx.coroutines.flow.Flow
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class LocalDataSource @Inject constructor(
//    private val database:AppDatabase
//) {
//    // RemoteKeys
//    private suspend fun insertRemoteKeys(remoteKey:List<RemoteKeys>) = database.remoteKeysDao().insertAll(remoteKey)
//    suspend fun getRemoteKeyById(id: String): RemoteKeys? = database.remoteKeysDao().getRemoteKeyById(id)
//    private suspend fun clearRemoteKeys() = database.remoteKeysDao().clearRemoteKeys()
//
//    // GameListItem
//    private suspend fun insertGameListItems(
//        games: List<GamesListItemEntity>
//    ) = database.gamesListDao().insertAll(games)
//    fun getAllGameListItems(): PagingSource<Int, GamesListItemEntity> = 
//        database.gamesListDao().getAll()
//    fun searchGameListItems(query: String): PagingSource<Int, GamesListItemEntity> = 
//        database.gamesListDao().search(query)
//    private suspend fun clearAllGameListItems() = database.gamesListDao().clearAll()
//    
//    // GameDetail
//    suspend fun insertGameDetail(
//        game: GameDetailEntity
//    ) = database.gameDetailDao().insert(game)
//    fun getGameDetail(id: Int) = database.gameDetailDao().getById(id)
//    
//    //GameCollections
//    suspend fun addCollection(
//        game: GamesCollectionEntity
//    ) = database.gameCollectionDao().insert(game)
//    suspend fun getAllCollections(limit: Int, offset: Int): List<GamesCollectionEntity> = 
//        database.gameCollectionDao().getAll(limit, offset)
//    suspend fun searchCollections(query: String, limit: Int, offset: Int): List<GamesCollectionEntity> =
//        database.gameCollectionDao().search(query, limit, offset)
//    suspend fun removeCollection(id: Int) = database.gameCollectionDao().remove(id)
//
//
//    suspend fun insertKeysAndGameListItems(
//        keys: List<RemoteKeys>,
//        gameListItems: List<GamesListItemEntity>,
//        isRefresh: Boolean = false
//    ){
//        database.withTransaction {
//            if (isRefresh) {
//                clearRemoteKeys()
//                clearAllGameListItems()
//            }
//            insertRemoteKeys(keys)
//            insertGameListItems(gameListItems)
//        }
//    }
//}
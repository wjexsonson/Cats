package com.example.cats.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(cat: CatEntity)

    @Query("DELETE FROM favorite_cats WHERE id = :id")
    suspend fun removeFromFavorites(id: String)

    @Query("DELETE FROM favorite_cats")
    suspend fun deleteAll()

    @Query("SELECT * FROM favorite_cats")
    fun getAllFavorites(): Flow<List<CatEntity>>

    @Query("SELECT id FROM favorite_cats")
    fun getFavoriteIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_cats WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean
}

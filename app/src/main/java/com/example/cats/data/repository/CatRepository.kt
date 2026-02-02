package com.example.cats.data.repository

import com.example.cats.data.db.CatDao
import com.example.cats.data.db.CatEntity
import com.example.cats.data.model.Cat
import com.example.cats.data.network.CatApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CatRepository @Inject constructor(
    private val apiService: CatApiService,
    private val catDao: CatDao
) {

    suspend fun getCats(page: Int): List<Cat> {
        val remoteCats = apiService.getCats(page)
        val favoriteIds = catDao.getFavoriteIds().firstOrNull() ?: emptyList()

        return remoteCats.map { cat ->
            cat.copy(isFavorite = favoriteIds.contains(cat.id))
        }
    }

    fun getFavoriteCats(): Flow<List<Cat>> {
        return catDao.getAllFavorites().map { entities ->
            entities.map { entity ->
                Cat(
                    id = entity.id,
                    url = entity.url,
                    isFavorite = true
                )
            }
        }
    }

    fun getFavoriteIds(): Flow<List<String>> {
        return catDao.getFavoriteIds()
    }

    suspend fun addToFavorites(cat: Cat) {
        catDao.addToFavorites(CatEntity(id = cat.id, url = cat.url))
    }

    suspend fun removeFromFavorites(id: String) {
        catDao.removeFromFavorites(id)
    }

    suspend fun clearAllFavorites() {
        catDao.deleteAll()
    }
}

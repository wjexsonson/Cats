package com.example.cats.data.network

import com.example.cats.data.model.Cat
import retrofit2.http.GET
import retrofit2.http.Query

interface CatApiService {
    @GET("v1/images/search?limit=50&size=small&order=ASC&mime_types=jpg,png")
    suspend fun getCats(
        @Query("page") page: Int
    ): List<Cat>
}

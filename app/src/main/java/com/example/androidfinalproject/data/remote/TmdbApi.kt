package com.example.androidfinalproject.data.remote

import com.example.androidfinalproject.data.model.TmdbSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApi {

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbSearchResponse
}

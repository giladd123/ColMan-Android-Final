package com.example.androidfinalproject.data.remote

import com.example.androidfinalproject.data.model.TmdbImagesResponse
import com.example.androidfinalproject.data.model.TmdbSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbSearchResponse

    @GET("movie/{movie_id}/images")
    suspend fun getMovieImages(
        @Path("movie_id") movieId: Int
    ): TmdbImagesResponse
}

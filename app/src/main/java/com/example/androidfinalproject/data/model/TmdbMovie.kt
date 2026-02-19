package com.example.androidfinalproject.data.model

import com.google.gson.annotations.SerializedName

data class TmdbSearchResponse(
    val results: List<TmdbMovie> = emptyList()
)

data class TmdbMovie(
    val id: Int = 0,
    val title: String = "",
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("release_date") val releaseDate: String = "",
    @SerializedName("genre_ids") val genreIds: List<Int> = emptyList(),
    val overview: String = ""
) {
    val posterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w342$it" }

    val backdropUrl: String?
        get() = backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }

    val year: String
        get() = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else ""
}

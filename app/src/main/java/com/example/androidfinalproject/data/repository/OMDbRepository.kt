package com.example.androidfinalproject.data.repository

import com.example.androidfinalproject.data.model.MovieDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class OMDbRepository(private val apiKey: String) {

    suspend fun getMovieDetails(title: String): Result<MovieDetails> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("OMDb API key is missing"))
        }
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Movie title is required"))
        }

        return withContext(Dispatchers.IO) {
            try {
                val encodedTitle = URLEncoder.encode(title, "UTF-8")
                val url = URL("http://www.omdbapi.com/?t=$encodedTitle&apikey=$apiKey")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext Result.failure(
                        IllegalStateException("OMDb request failed with $responseCode")
                    )
                }

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                val isSuccess = json.optString("Response", "False") == "True"
                if (!isSuccess) {
                    val error = json.optString("Error", "Movie not found")
                    return@withContext Result.failure(IllegalStateException(error))
                }

                val posterUrl = json.optString("Poster", "")
                val imdbRating = json.optString("imdbRating", "0").toFloatOrNull() ?: 0f
                val movieTitle = json.optString("Title", title)

                Result.success(
                    MovieDetails(
                        title = movieTitle,
                        posterUrl = posterUrl,
                        imdbRating = imdbRating
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

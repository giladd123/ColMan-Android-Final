package com.example.androidfinalproject.ui.addreview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.androidfinalproject.data.local.AppDatabase
import com.example.androidfinalproject.data.model.Review
import com.example.androidfinalproject.data.model.TmdbMovie
import com.example.androidfinalproject.data.remote.TmdbClient
import com.example.androidfinalproject.data.remote.TmdbGenres
import com.example.androidfinalproject.data.repository.ReviewRepository
import com.example.androidfinalproject.data.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReviewRepository
    private val userProfileRepository: UserProfileRepository

    private val _searchResults = MutableLiveData<List<TmdbMovie>>()
    val searchResults: LiveData<List<TmdbMovie>> = _searchResults

    private val _selectedMovie = MutableLiveData<TmdbMovie?>()
    val selectedMovie: LiveData<TmdbMovie?> = _selectedMovie

    private val _isSearching = MutableLiveData<Boolean>()
    val isSearching: LiveData<Boolean> = _isSearching

    private val _isSubmitting = MutableLiveData<Boolean>()
    val isSubmitting: LiveData<Boolean> = _isSubmitting

    private val _submitResult = MutableLiveData<Result<Review>?>()
    val submitResult: LiveData<Result<Review>?> = _submitResult

    private var searchJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ReviewRepository(database.reviewDao())
        userProfileRepository = UserProfileRepository(database.userProfileDao())
    }

    fun searchMovies(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _isSearching.value = true
            try {
                val response = TmdbClient.api.searchMovies(query)
                _searchResults.value = response.results
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun selectMovie(movie: TmdbMovie) {
        _selectedMovie.value = movie
        _searchResults.value = emptyList()
    }

    fun clearSelectedMovie() {
        _selectedMovie.value = null
    }

    fun submitReview(rating: Float, reviewText: String) {
        val movie = _selectedMovie.value ?: return
        viewModelScope.launch {
            _isSubmitting.value = true
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid ?: ""

            // Fetch the user's profile picture URL
            var profilePictureUrl = ""
            if (userId.isNotEmpty()) {
                try {
                    val profileResult = userProfileRepository.refreshProfile(userId)
                    if (profileResult.isSuccess) {
                        profilePictureUrl = profileResult.getOrNull()?.photoUrl ?: ""
                    }
                } catch (_: Exception) { }
            }

            val review = Review(
                movieTitle = movie.title,
                movieBannerUrl = movie.backdropUrl ?: "",
                movieGenre = TmdbGenres.getGenreNames(movie.genreIds),
                rating = rating,
                reviewText = reviewText,
                userId = userId,
                userFullName = user?.displayName ?: "",
                userProfilePictureUrl = profilePictureUrl
            )
            val result = repository.addReview(review)
            _submitResult.value = result
            _isSubmitting.value = false
        }
    }

    fun clearSubmitResult() {
        _submitResult.value = null
    }
}

package com.example.androidfinalproject.ui.addreview

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.androidfinalproject.BuildConfig
import com.example.androidfinalproject.data.local.AppDatabase
import com.example.androidfinalproject.data.model.MovieDetails
import com.example.androidfinalproject.data.model.Review
import com.example.androidfinalproject.data.repository.OMDbRepository
import com.example.androidfinalproject.data.repository.ReviewRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    private val reviewRepository: ReviewRepository
    private val omdbRepository: OMDbRepository

    private var fetchJob: Job? = null
    private var lastQueriedTitle: String = ""

    private val _addReviewState = MutableLiveData<Result<Review>?>()
    val addReviewState: LiveData<Result<Review>?> = _addReviewState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isUploadingImage = MutableLiveData<Boolean>()
    val isUploadingImage: LiveData<Boolean> = _isUploadingImage

    private val _uploadedImageUrl = MutableLiveData<String?>()
    val uploadedImageUrl: LiveData<String?> = _uploadedImageUrl

    private val _movieDetails = MutableLiveData<MovieDetails?>()
    val movieDetails: LiveData<MovieDetails?> = _movieDetails

    private val _suggestedRating = MutableLiveData<Float?>()
    val suggestedRating: LiveData<Float?> = _suggestedRating

    private val _isFetchingMovie = MutableLiveData<Boolean>()
    val isFetchingMovie: LiveData<Boolean> = _isFetchingMovie

    init {
        val database = AppDatabase.getDatabase(application)
        reviewRepository = ReviewRepository(database.reviewDao())
        omdbRepository = OMDbRepository(BuildConfig.OMDB_API_KEY)
    }

    fun onMovieTitleChanged(title: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) {
            fetchJob?.cancel()
            lastQueriedTitle = ""
            _movieDetails.value = null
            _suggestedRating.value = null
            _isFetchingMovie.value = false
            return
        }
        if (trimmedTitle.equals(lastQueriedTitle, ignoreCase = true)) {
            return
        }

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            delay(400)
            lastQueriedTitle = trimmedTitle
            _isFetchingMovie.value = true
            val result = omdbRepository.getMovieDetails(trimmedTitle)
            if (result.isSuccess) {
                val details = result.getOrNull()
                _movieDetails.value = details
                _suggestedRating.value = details?.let { ratingToFiveStar(it.imdbRating) }
            } else {
                _movieDetails.value = null
                _suggestedRating.value = null
                _error.value = result.exceptionOrNull()?.message ?: "Failed to fetch movie details"
            }
            _isFetchingMovie.value = false
        }
    }

    fun uploadReviewImage(imageUri: Uri) {
        _isUploadingImage.value = true
        viewModelScope.launch {
            try {
                val result = reviewRepository.uploadReviewImage(imageUri, currentUserId)
                if (result.isSuccess) {
                    _uploadedImageUrl.value = result.getOrNull()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to upload image"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isUploadingImage.value = false
            }
        }
    }

    fun createReview(
        movieTitle: String,
        movieBannerUrl: String,
        rating: Float,
        reviewText: String
    ) {
        if (_isFetchingMovie.value == true) {
            _error.value = "Please wait for movie details to finish loading"
            return
        }
        if (movieTitle.isBlank()) {
            _error.value = "Movie title is required"
            return
        }
        if (reviewText.isBlank()) {
            _error.value = "Review text is required"
            return
        }
        if (rating <= 0f) {
            _error.value = "Please provide a rating"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val userFullName = auth.currentUser?.displayName ?: "Anonymous"

                val review = Review(
                    id = "",
                    movieTitle = movieTitle,
                    movieBannerUrl = movieBannerUrl,
                    rating = rating,
                    reviewText = reviewText,
                    userId = currentUserId,
                    userFullName = userFullName,
                    timestamp = System.currentTimeMillis()
                )

                val result = reviewRepository.addReview(review)
                _addReviewState.value = result
            } catch (e: Exception) {
                _error.value = e.message
                _addReviewState.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearState() {
        _addReviewState.value = null
        _uploadedImageUrl.value = null
        _movieDetails.value = null
        _suggestedRating.value = null
        _error.value = null
        _isFetchingMovie.value = false
        fetchJob?.cancel()
    }

    private fun ratingToFiveStar(imdbRating: Float): Float {
        if (imdbRating <= 0f) return 0f
        val rating = imdbRating / 2f
        return rating.coerceIn(0f, 5f)
    }
}


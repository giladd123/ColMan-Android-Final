package com.example.androidfinalproject.ui.addreview

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.androidfinalproject.data.local.AppDatabase
import com.example.androidfinalproject.data.model.Review
import com.example.androidfinalproject.data.repository.ReviewRepository
import com.example.androidfinalproject.data.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AddReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    private val reviewRepository: ReviewRepository
    private val userProfileRepository: UserProfileRepository

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

    init {
        val database = AppDatabase.getDatabase(application)
        reviewRepository = ReviewRepository(database.reviewDao())
        userProfileRepository = UserProfileRepository(database.userProfileDao())
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
        _error.value = null
    }
}


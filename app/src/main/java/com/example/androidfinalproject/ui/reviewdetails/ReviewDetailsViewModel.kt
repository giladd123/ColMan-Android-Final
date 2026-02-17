package com.example.androidfinalproject.ui.reviewdetails

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.androidfinalproject.data.local.AppDatabase
import com.example.androidfinalproject.data.model.Review
import com.example.androidfinalproject.data.repository.ReviewRepository
import kotlinx.coroutines.launch

class ReviewDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val reviewRepository: ReviewRepository
    
    private val _review = MutableLiveData<Review?>()
    val review: LiveData<Review?> = _review
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _updateSuccess = MutableLiveData<Boolean?>()
    val updateSuccess: LiveData<Boolean?> = _updateSuccess

    private val _uploadSuccess = MutableLiveData<Boolean?>()
    val uploadSuccess: LiveData<Boolean?> = _uploadSuccess

    init {
        val reviewDao = AppDatabase.getDatabase(application).reviewDao()
        reviewRepository = ReviewRepository(reviewDao)
    }

    fun loadReview(reviewId: String) {
        reviewRepository.getReviewById(reviewId).observeForever { review ->
            _review.value = review
        }
    }

    fun updateReview(review: Review) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = reviewRepository.updateReview(review)
                if (result.isSuccess) {
                    _updateSuccess.value = true
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to update review"
                    _updateSuccess.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _updateSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadReviewImage(imageUri: Uri) {
        val currentReview = _review.value
        if (currentReview != null) {
            viewModelScope.launch {
                try {
                    val result = reviewRepository.uploadReviewImage(imageUri, currentReview.userId)
                    if (result.isSuccess) {
                        _uploadSuccess.value = true
                    } else {
                        _error.value = result.exceptionOrNull()?.message ?: "Failed to upload image"
                        _uploadSuccess.value = false
                    }
                } catch (e: Exception) {
                    _error.value = e.message
                    _uploadSuccess.value = false
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearUpdateStatus() {
        _updateSuccess.value = null
    }

    fun clearUploadStatus() {
        _uploadSuccess.value = null
    }
}

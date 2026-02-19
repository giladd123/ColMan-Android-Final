package com.example.androidfinalproject.ui.reviewdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.androidfinalproject.data.local.AppDatabase
import com.example.androidfinalproject.data.model.Review
import com.example.androidfinalproject.data.repository.ReviewRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ReviewDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val reviewRepository: ReviewRepository

    private val _deleteResult = MutableLiveData<Result<Unit>?>()
    val deleteResult: LiveData<Result<Unit>?> = _deleteResult

    private val _updateResult = MutableLiveData<Result<Unit>?>()
    val updateResult: LiveData<Result<Unit>?> = _updateResult

    init {
        val db = AppDatabase.getDatabase(application)
        reviewRepository = ReviewRepository(db.reviewDao())
    }

    fun getReview(reviewId: String): LiveData<Review?> {
        return reviewRepository.getReviewById(reviewId)
    }

    fun isOwner(review: Review): Boolean {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        return review.userId == currentUserId
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            _deleteResult.value = reviewRepository.deleteReview(reviewId)
        }
    }

    fun updateReview(reviewId: String, rating: Float, reviewText: String) {
        viewModelScope.launch {
            _updateResult.value = reviewRepository.updateReview(reviewId, rating, reviewText)
        }
    }
}

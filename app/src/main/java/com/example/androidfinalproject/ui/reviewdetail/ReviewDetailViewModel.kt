package com.example.androidfinalproject.ui.reviewdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.androidfinalproject.data.local.AppDatabase
import com.example.androidfinalproject.data.model.Review
import com.example.androidfinalproject.data.model.TmdbBackdrop
import com.example.androidfinalproject.data.remote.TmdbClient
import com.example.androidfinalproject.data.repository.ReviewRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ReviewDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val reviewRepository: ReviewRepository

    private val _deleteResult = MutableLiveData<Result<Unit>?>()
    val deleteResult: LiveData<Result<Unit>?> = _deleteResult

    private val _updateResult = MutableLiveData<Result<Unit>?>()
    val updateResult: LiveData<Result<Unit>?> = _updateResult

    private val _backdrops = MutableLiveData<List<TmdbBackdrop>>()
    val backdrops: LiveData<List<TmdbBackdrop>> = _backdrops

    private val _isLoadingBackdrops = MutableLiveData<Boolean>()
    val isLoadingBackdrops: LiveData<Boolean> = _isLoadingBackdrops

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

    fun updateReview(reviewId: String, rating: Float, reviewText: String, movieBannerUrl: String) {
        viewModelScope.launch {
            _updateResult.value = reviewRepository.updateReview(reviewId, rating, reviewText, movieBannerUrl)
        }
    }

    fun fetchBackdropsForMovie(movieTmdbId: Int) {
        if (movieTmdbId == 0) {
            _backdrops.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isLoadingBackdrops.value = true
            try {
                val response = TmdbClient.api.getMovieImages(movieTmdbId)
                _backdrops.value = response.backdrops
            } catch (e: Exception) {
                _backdrops.value = emptyList()
            } finally {
                _isLoadingBackdrops.value = false
            }
        }
    }
}

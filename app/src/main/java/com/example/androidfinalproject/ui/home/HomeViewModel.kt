package com.example.androidfinalproject.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.androidfinalproject.data.local.AppDatabase
import com.example.androidfinalproject.data.model.Review
import com.example.androidfinalproject.data.repository.ReviewRepository
import com.example.androidfinalproject.data.util.ReviewSeeder
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ReviewRepository

    val reviews: LiveData<List<Review>>

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _seedingComplete = MutableLiveData<Boolean?>()
    val seedingComplete: LiveData<Boolean?> = _seedingComplete

    init {
        val reviewDao = AppDatabase.getDatabase(application).reviewDao()
        repository = ReviewRepository(reviewDao)
        reviews = repository.allReviews
        refreshReviews()
    }

    fun refreshReviews() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.refreshReviews()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun seedExampleReviews() {
        _isLoading.value = true
        ReviewSeeder.seedReviews { success ->
            _seedingComplete.postValue(success)
            if (success) {
                refreshReviews()
            } else {
                _isLoading.postValue(false)
            }
        }
    }

    fun clearSeedingStatus() {
        _seedingComplete.value = null
    }
}


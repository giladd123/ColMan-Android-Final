package com.example.androidfinalproject.ui.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.androidfinalproject.data.local.AppDatabase
import com.example.androidfinalproject.data.model.Review
import com.example.androidfinalproject.data.model.UserProfile
import com.example.androidfinalproject.data.repository.AuthRepository
import com.example.androidfinalproject.data.repository.ReviewRepository
import com.example.androidfinalproject.data.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid ?: ""

    private val authRepository: AuthRepository
    private val userProfileRepository: UserProfileRepository
    private val reviewRepository: ReviewRepository

    val profile: LiveData<UserProfile?>
    val userReviews: LiveData<List<Review>>

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSaving = MutableLiveData<Boolean>()
    val isSaving: LiveData<Boolean> = _isSaving

    private val _isUploadingPhoto = MutableLiveData<Boolean>()
    val isUploadingPhoto: LiveData<Boolean> = _isUploadingPhoto

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _saveSuccess = MutableLiveData<Boolean?>()
    val saveSuccess: LiveData<Boolean?> = _saveSuccess

    private val _uploadedPhotoUrl = MutableLiveData<String?>()
    val uploadedPhotoUrl: LiveData<String?> = _uploadedPhotoUrl

    init {
        val database = AppDatabase.getDatabase(application)
        authRepository = AuthRepository()
        userProfileRepository = UserProfileRepository(database.userProfileDao())
        reviewRepository = ReviewRepository(database.reviewDao())

        profile = userProfileRepository.getProfile(currentUserId)
        userReviews = reviewRepository.getReviewsByUserId(currentUserId)

        loadProfile()
    }

    private fun loadProfile() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                userProfileRepository.refreshProfile(currentUserId)
                reviewRepository.refreshReviewsForUser(currentUserId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadProfilePicture(imageUri: Uri) {
        _isUploadingPhoto.value = true
        viewModelScope.launch {
            try {
                val result = userProfileRepository.uploadProfilePicture(currentUserId, imageUri)
                if (result.isSuccess) {
                    _uploadedPhotoUrl.value = result.getOrNull()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to upload image"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isUploadingPhoto.value = false
            }
        }
    }

    fun saveProfile(fullName: String, photoUrl: String?) {
        if (fullName.isBlank()) {
            _error.value = "Name is required"
            return
        }

        _isSaving.value = true
        viewModelScope.launch {
            try {
                val result = userProfileRepository.updateProfile(currentUserId, fullName, photoUrl)
                if (result.isSuccess) {
                    // Update review author names and profile picture in background
                    reviewRepository.updateUserFullNameForAllReviews(currentUserId, fullName)
                    if (photoUrl != null) {
                        reviewRepository.updateUserProfilePictureForAllReviews(currentUserId, photoUrl)
                    }
                    _saveSuccess.value = true
                } else {
                    _error.value = result.exceptionOrNull()?.message
                    _saveSuccess.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _saveSuccess.value = false
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearSaveStatus() {
        _saveSuccess.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun clearUploadedPhotoUrl() {
        _uploadedPhotoUrl.value = null
    }

}

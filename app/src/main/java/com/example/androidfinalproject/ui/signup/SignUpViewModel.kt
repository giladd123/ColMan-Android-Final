package com.example.androidfinalproject.ui.signup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.androidfinalproject.data.local.AppDatabase
import com.example.androidfinalproject.data.repository.AuthRepository
import com.example.androidfinalproject.data.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val userProfileRepository: UserProfileRepository

    private val _signUpResult = MutableLiveData<Result<FirebaseUser>?>()
    val signUpResult: LiveData<Result<FirebaseUser>?> = _signUpResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var pendingPhotoUri: Uri? = null

    init {
        val database = AppDatabase.getDatabase(application)
        userProfileRepository = UserProfileRepository(database.userProfileDao())
    }

    fun setPhotoUri(uri: Uri) {
        pendingPhotoUri = uri
    }

    fun signUp(email: String, password: String, displayName: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.signUpWithEmail(email, password, displayName)
            if (result.isSuccess) {
                val user = result.getOrNull()!!
                val photoUri = pendingPhotoUri
                if (photoUri != null) {
                    val uploadResult = userProfileRepository.uploadProfilePicture(user.uid, photoUri)
                    userProfileRepository.updateProfile(user.uid, displayName, uploadResult.getOrNull())
                }
            }
            _signUpResult.value = result
            _isLoading.value = false
        }
    }

    fun clearResult() {
        _signUpResult.value = null
    }
}

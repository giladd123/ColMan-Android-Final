package com.example.androidfinalproject.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfinalproject.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _signUpResult = MutableLiveData<Result<FirebaseUser>?>()
    val signUpResult: LiveData<Result<FirebaseUser>?> = _signUpResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun signUp(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.signUpWithEmail(email, password)
            _signUpResult.value = result
            _isLoading.value = false
        }
    }

    fun clearResult() {
        _signUpResult.value = null
    }
}

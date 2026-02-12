package com.example.androidfinalproject.ui.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidfinalproject.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class SignInViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _signInResult = MutableLiveData<Result<FirebaseUser>?>()
    val signInResult: LiveData<Result<FirebaseUser>?> = _signInResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun signIn(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, password)
            _signInResult.value = result
            _isLoading.value = false
        }
    }

    fun clearResult() {
        _signInResult.value = null
    }
}

package com.example.androidfinalproject.ui.login

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidfinalproject.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser

class LoginViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _loginResult = MutableLiveData<Result<FirebaseUser>?>()
    val loginResult: LiveData<Result<FirebaseUser>?> = _loginResult

    val currentUser: FirebaseUser?
        get() = authRepository.currentUser

    fun buildGoogleSignInIntent(): Intent {
        return authRepository.buildGoogleSignInIntent()
    }

    fun onSignInSuccess() {
        authRepository.currentUser?.let {
            _loginResult.value = Result.success(it)
        } ?: run {
            _loginResult.value = Result.failure(Exception("Sign-in returned null user"))
        }
    }

    fun onSignInFailed(error: String) {
        _loginResult.value = Result.failure(Exception(error))
    }

    fun clearLoginResult() {
        _loginResult.value = null
    }
}

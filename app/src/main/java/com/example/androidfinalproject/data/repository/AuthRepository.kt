package com.example.androidfinalproject.data.repository

import android.content.Intent
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun buildGoogleSignInIntent(): Intent {
        val providers = listOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
    }

    fun signOut() {
        firebaseAuth.signOut()
        AuthUI.getInstance().signOut(firebaseAuth.app.applicationContext)
    }
}

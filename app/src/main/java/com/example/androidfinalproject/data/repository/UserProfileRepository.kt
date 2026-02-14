package com.example.androidfinalproject.data.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.androidfinalproject.data.local.UserProfileDao
import com.example.androidfinalproject.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import androidx.core.net.toUri

class UserProfileRepository(private val userProfileDao: UserProfileDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getProfile(uid: String): LiveData<UserProfile?> {
        return userProfileDao.getProfile(uid)
    }

    suspend fun refreshProfile(uid: String): Result<UserProfile> {
        return try {
            val docSnapshot = usersCollection.document(uid).get().await()
            
            val profile = if (docSnapshot.exists()) {
                docSnapshot.toObject(UserProfile::class.java)?.copy(uid = uid)
                    ?: createDefaultProfile(uid)
            } else {
                // Create default profile if it doesn't exist
                val defaultProfile = createDefaultProfile(uid)
                usersCollection.document(uid).set(defaultProfile).await()
                defaultProfile
            }

            userProfileDao.insertProfile(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePicture(uid: String, imageUri: Uri): Result<String> {
        return try {
            val storageRef = storage.reference
            val profilePicRef = storageRef.child("profile_pictures/${uid}.jpg")
            
            profilePicRef.putFile(imageUri).await()
            val downloadUrl = profilePicRef.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(uid: String, fullName: String, photoUrl: String?): Result<UserProfile> {
        return try {
            val profile = UserProfile(
                uid = uid,
                fullName = fullName,
                photoUrl = photoUrl ?: "",
                updatedAt = System.currentTimeMillis()
            )

            // Update Firestore
            usersCollection.document(uid).set(profile).await()

            // Update Room cache
            userProfileDao.insertProfile(profile)

            // Update FirebaseAuth profile for consistency
            auth.currentUser?.let { user ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .apply {
                        if (!photoUrl.isNullOrEmpty()) {
                            photoUri = photoUrl.toUri()
                        }
                    }
                    .build()
                user.updateProfile(profileUpdates).await()
            }

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createDefaultProfile(uid: String): UserProfile {
        val firebaseUser = auth.currentUser
        return UserProfile(
            uid = uid,
            fullName = firebaseUser?.displayName ?: "",
            photoUrl = firebaseUser?.photoUrl?.toString() ?: "",
            updatedAt = System.currentTimeMillis()
        )
    }
}

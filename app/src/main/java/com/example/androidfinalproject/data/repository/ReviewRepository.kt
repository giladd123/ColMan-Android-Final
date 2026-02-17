package com.example.androidfinalproject.data.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.androidfinalproject.data.local.ReviewDao
import com.example.androidfinalproject.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ReviewRepository(private val reviewDao: ReviewDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")
    private val storage = FirebaseStorage.getInstance()

    val allReviews: LiveData<List<Review>> = reviewDao.getAllReviews()

    fun getReviewsByUserId(userId: String): LiveData<List<Review>> {
        return reviewDao.getReviewsByUserId(userId)
    }

    fun getReviewById(reviewId: String): LiveData<Review> {
        return reviewDao.getReviewById(reviewId)
    }

    suspend fun refreshReviews() {
        try {
            val snapshot = reviewsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val reviews = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Review::class.java)?.copy(id = doc.id)
            }

            reviewDao.deleteAllReviews()
            reviewDao.insertReviews(reviews)
        } catch (e: Exception) {
            // If network fails, we still have cached data from Room
            e.printStackTrace()
        }
    }

    suspend fun refreshReviewsForUser(userId: String) {
        try {
            val snapshot = reviewsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val reviews = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Review::class.java)?.copy(id = doc.id)
            }

            reviews.forEach { review ->
                reviewDao.insertReview(review)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addReview(review: Review): Result<Review> {
        return try {
            val docRef = reviewsCollection.add(review).await()
            val newReview = review.copy(id = docRef.id)
            reviewDao.insertReview(newReview)
            Result.success(newReview)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserFullNameForAllReviews(userId: String, newFullName: String): Result<Unit> {
        return try {
            // Update Firestore in batches (max 500 writes per batch)
            var snapshot = reviewsCollection
                .whereEqualTo("userId", userId)
                .limit(500)
                .get()
                .await()

            while (snapshot.documents.isNotEmpty()) {
                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "userFullName", newFullName)
                }
                batch.commit().await()

                // Get next batch if needed
                val lastDoc = snapshot.documents.lastOrNull()
                if (snapshot.documents.size < 500) break
                
                snapshot = reviewsCollection
                    .whereEqualTo("userId", userId)
                    .startAfter(lastDoc)
                    .limit(500)
                    .get()
                    .await()
            }

            // Update Room cache
            reviewDao.updateUserFullNameForUser(userId, newFullName)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadReviewImage(imageUri: Uri, userId: String): Result<String> {
        return try {
            val storageRef = storage.reference
            val reviewImageRef = storageRef.child("review_images/${userId}/${System.currentTimeMillis()}.jpg")

            reviewImageRef.putFile(imageUri).await()
            val downloadUrl = reviewImageRef.downloadUrl.await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReview(review: Review): Result<Review> {
        return try {
            reviewsCollection.document(review.id).set(review).await()
            reviewDao.insertReview(review)
            Result.success(review)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

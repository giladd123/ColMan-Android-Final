package com.example.androidfinalproject.data.repository

import androidx.lifecycle.LiveData
import com.example.androidfinalproject.data.local.ReviewDao
import com.example.androidfinalproject.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ReviewRepository(private val reviewDao: ReviewDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollection = firestore.collection("reviews")

    val allReviews: LiveData<List<Review>> = reviewDao.getAllReviews()

    fun getReviewsByUserId(userId: String): LiveData<List<Review>> {
        return reviewDao.getReviewsByUserId(userId)
    }

    fun getReviewById(reviewId: String): LiveData<Review?> {
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

    suspend fun deleteReview(reviewId: String): Result<Unit> {
        return try {
            reviewsCollection.document(reviewId).delete().await()
            reviewDao.deleteReviewById(reviewId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReview(reviewId: String, rating: Float, reviewText: String): Result<Unit> {
        return try {
            reviewsCollection.document(reviewId).update(
                mapOf("rating" to rating, "reviewText" to reviewText)
            ).await()
            reviewDao.updateReview(reviewId, rating, reviewText)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReview(reviewId: String, rating: Float, reviewText: String, movieBannerUrl: String): Result<Unit> {
        return try {
            reviewsCollection.document(reviewId).update(
                mapOf("rating" to rating, "reviewText" to reviewText, "movieBannerUrl" to movieBannerUrl)
            ).await()
            reviewDao.updateReviewWithBanner(reviewId, rating, reviewText, movieBannerUrl)
            Result.success(Unit)
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

    suspend fun updateUserProfilePictureForAllReviews(userId: String, newPhotoUrl: String): Result<Unit> {
        return try {
            var snapshot = reviewsCollection
                .whereEqualTo("userId", userId)
                .limit(500)
                .get()
                .await()

            while (snapshot.documents.isNotEmpty()) {
                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "userProfilePictureUrl", newPhotoUrl)
                }
                batch.commit().await()

                val lastDoc = snapshot.documents.lastOrNull()
                if (snapshot.documents.size < 500) break

                snapshot = reviewsCollection
                    .whereEqualTo("userId", userId)
                    .startAfter(lastDoc)
                    .limit(500)
                    .get()
                    .await()
            }

            reviewDao.updateUserProfilePictureForUser(userId, newPhotoUrl)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

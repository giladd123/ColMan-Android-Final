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
}

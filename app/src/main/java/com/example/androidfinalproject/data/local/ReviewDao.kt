package com.example.androidfinalproject.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.androidfinalproject.data.model.Review

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviews(): LiveData<List<Review>>

    @Query("SELECT * FROM reviews WHERE userId = :userId ORDER BY timestamp DESC")
    fun getReviewsByUserId(userId: String): LiveData<List<Review>>

    @Query("SELECT * FROM reviews WHERE id = :reviewId")
    fun getReviewById(reviewId: String): LiveData<Review?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    @Query("DELETE FROM reviews")
    suspend fun deleteAllReviews()

    @Query("UPDATE reviews SET userFullName = :newFullName WHERE userId = :userId")
    suspend fun updateUserFullNameForUser(userId: String, newFullName: String)

    @Query("UPDATE reviews SET userProfilePictureUrl = :newPhotoUrl WHERE userId = :userId")
    suspend fun updateUserProfilePictureForUser(userId: String, newPhotoUrl: String)

    @Query("DELETE FROM reviews WHERE id = :reviewId")
    suspend fun deleteReviewById(reviewId: String)

    @Query("UPDATE reviews SET rating = :rating, reviewText = :reviewText WHERE id = :reviewId")
    suspend fun updateReview(reviewId: String, rating: Float, reviewText: String)
}

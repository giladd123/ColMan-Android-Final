package com.example.androidfinalproject.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey
    val id: String = "",
    val movieTitle: String = "",
    val movieBannerUrl: String = "",
    val movieGenre: String = "",
    val rating: Float = 0f,
    val reviewText: String = "",
    val userId: String = "",
    val userFullName: String = "",
    val userProfilePictureUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

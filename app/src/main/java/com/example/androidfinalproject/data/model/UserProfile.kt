package com.example.androidfinalproject.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val uid: String = "",
    val fullName: String = "",
    val photoUrl: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

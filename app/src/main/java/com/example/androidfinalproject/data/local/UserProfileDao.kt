package com.example.androidfinalproject.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.androidfinalproject.data.model.UserProfile

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE uid = :uid")
    fun getProfile(uid: String): LiveData<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Query("DELETE FROM user_profiles WHERE uid = :uid")
    suspend fun deleteProfile(uid: String)
}

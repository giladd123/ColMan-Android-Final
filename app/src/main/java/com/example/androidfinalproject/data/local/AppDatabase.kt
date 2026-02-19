package com.example.androidfinalproject.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.androidfinalproject.data.model.Review
import com.example.androidfinalproject.data.model.UserProfile

@Database(entities = [Review::class, UserProfile::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reviewDao(): ReviewDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "movierate_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

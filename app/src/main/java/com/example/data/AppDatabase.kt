package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Album::class, SavedPhoto::class], version = 1, exportSchema = false)
abstract class SociallyDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun savedPhotoDao(): SavedPhotoDao

    companion object {
        @Volatile
        private var INSTANCE: SociallyDatabase? = null

        fun getDatabase(context: Context): SociallyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SociallyDatabase::class.java,
                    "socially_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

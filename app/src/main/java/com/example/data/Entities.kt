package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "albums")
data class Album(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val coverPhotoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "saved_photos")
data class SavedPhoto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val albumId: Int? = null,
    val title: String,
    val caption: String? = null,
    val fileUri: String,
    val filterName: String = "Normal",
    val brightness: Float = 0f,    // visual adjustment: range -1f to 1f
    val contrast: Float = 1f,      // visual adjustment: range 0.5f to 1.5f
    val saturation: Float = 1f,    // visual adjustment: range 0f to 2f
    val warmth: Float = 0f,        // visual adjustment: range -0.5f to 0.5f
    val vignette: Float = 0f,      // visual adjustment: range 0f to 1f
    val sharpness: Float = 0f,     // visual adjustment: range 0f to 1f
    val syncStatus: String = "Pending", // "Pending" or "Synced"
    val savedAt: Long = System.currentTimeMillis()
) : Serializable

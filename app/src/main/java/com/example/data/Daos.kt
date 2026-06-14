package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums ORDER BY createdAt DESC")
    fun getAllAlbums(): Flow<List<Album>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: Album): Long

    @Delete
    suspend fun deleteAlbum(album: Album)

    @Query("DELETE FROM albums WHERE id = :id")
    suspend fun deleteAlbumById(id: Int)
}

@Dao
interface SavedPhotoDao {
    @Query("SELECT * FROM saved_photos ORDER BY savedAt DESC")
    fun getAllSavedPhotos(): Flow<List<SavedPhoto>>

    @Query("SELECT * FROM saved_photos WHERE albumId = :albumId ORDER BY savedAt DESC")
    fun getPhotosInAlbum(albumId: Int): Flow<List<SavedPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: SavedPhoto): Long

    @Delete
    suspend fun deletePhoto(photo: SavedPhoto)

    @Query("DELETE FROM saved_photos WHERE id = :id")
    suspend fun deletePhotoById(id: Int)

    @Query("UPDATE saved_photos SET syncStatus = 'Synced' WHERE syncStatus = 'Pending'")
    suspend fun markAllAsSynced()

    @Query("SELECT COUNT(*) FROM saved_photos WHERE syncStatus = 'Pending'")
    fun getPendingCount(): Flow<Int>
}

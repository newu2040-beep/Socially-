package com.example.data

import kotlinx.coroutines.flow.Flow

class SociallyRepository(private val database: SociallyDatabase) {
    val albumDao = database.albumDao()
    val savedPhotoDao = database.savedPhotoDao()

    val allAlbums: Flow<List<Album>> = albumDao.getAllAlbums()
    val allPhotos: Flow<List<SavedPhoto>> = savedPhotoDao.getAllSavedPhotos()
    val pendingCount: Flow<Int> = savedPhotoDao.getPendingCount()

    fun getPhotosInAlbum(albumId: Int): Flow<List<SavedPhoto>> {
        return savedPhotoDao.getPhotosInAlbum(albumId)
    }

    suspend fun insertAlbum(album: Album): Long {
        return albumDao.insertAlbum(album)
    }

    suspend fun deleteAlbum(album: Album) {
        albumDao.deleteAlbum(album)
    }

    suspend fun insertPhoto(photo: SavedPhoto): Long {
        return savedPhotoDao.insertPhoto(photo)
    }

    suspend fun deletePhoto(photo: SavedPhoto) {
        savedPhotoDao.deletePhoto(photo)
    }

    suspend fun markAllAsSynced() {
        savedPhotoDao.markAllAsSynced()
    }
}

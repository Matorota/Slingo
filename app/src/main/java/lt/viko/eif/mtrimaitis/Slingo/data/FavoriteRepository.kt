package lt.viko.eif.mtrimaitis.Slingo.data

import kotlinx.coroutines.flow.Flow
import lt.viko.eif.mtrimaitis.Slingo.data.dao.FavoriteDao
import lt.viko.eif.mtrimaitis.Slingo.data.models.Song

class FavoriteRepository(private val favoriteDao: FavoriteDao) {
    fun getFavoriteSongs(): Flow<List<Song>> {
        return favoriteDao.getFavoriteSongs()
    }

    suspend fun isFavorite(songId: String): Boolean {
        return favoriteDao.isFavorite(songId) != null
    }

    suspend fun addFavorite(songId: String) {
        val favorite = lt.viko.eif.mtrimaitis.Slingo.data.models.FavoriteSong(songId = songId)
        favoriteDao.addFavorite(favorite)
    }

    suspend fun removeFavorite(songId: String) {
        favoriteDao.removeFavoriteBySongId(songId)
    }

    suspend fun toggleFavorite(songId: String): Boolean {
        val isCurrentlyFavorite = isFavorite(songId)
        if (isCurrentlyFavorite) {
            removeFavorite(songId)
        } else {
            addFavorite(songId)
        }
        return !isCurrentlyFavorite
    }
}


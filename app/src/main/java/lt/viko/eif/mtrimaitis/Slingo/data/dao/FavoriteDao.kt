package lt.viko.eif.mtrimaitis.Slingo.data.dao

import androidx.room.*
import lt.viko.eif.mtrimaitis.Slingo.data.models.FavoriteSong
import lt.viko.eif.mtrimaitis.Slingo.data.models.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_songs")
    fun getAllFavorites(): Flow<List<FavoriteSong>>

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN favorite_songs fs ON s.id = fs.songId
        ORDER BY fs.addedAt DESC
    """)
    fun getFavoriteSongs(): Flow<List<Song>>

    @Query("SELECT * FROM favorite_songs WHERE songId = :songId LIMIT 1")
    suspend fun isFavorite(songId: String): FavoriteSong?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favoriteSong: FavoriteSong)

    @Delete
    suspend fun removeFavorite(favoriteSong: FavoriteSong)

    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun removeFavoriteBySongId(songId: String)
}


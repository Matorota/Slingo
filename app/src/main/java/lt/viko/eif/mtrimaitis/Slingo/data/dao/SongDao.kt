package lt.viko.eif.mtrimaitis.Slingo.data.dao

import androidx.room.*
import lt.viko.eif.mtrimaitis.Slingo.data.models.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: String): Song?

    @Query("SELECT * FROM songs WHERE name LIKE :query OR artist LIKE :query")
    fun searchSongs(query: String): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Query("SELECT * FROM songs ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomSongs(limit: Int): List<Song>

    @Delete
    suspend fun deleteSong(song: Song)
}


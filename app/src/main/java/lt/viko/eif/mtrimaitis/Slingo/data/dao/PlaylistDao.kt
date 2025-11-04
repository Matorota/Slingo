package lt.viko.eif.mtrimaitis.Slingo.data.dao

import androidx.room.*
import lt.viko.eif.mtrimaitis.Slingo.data.models.Playlist
import lt.viko.eif.mtrimaitis.Slingo.data.models.PlaylistSong
import lt.viko.eif.mtrimaitis.Slingo.data.models.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists WHERE userId = :userId")
    fun getPlaylistsByUser(userId: Long): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: Long): Playlist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId")
    fun getPlaylistSongs(playlistId: Long): Flow<List<PlaylistSong>>

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON s.id = ps.songId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.addedAt
    """)
    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(playlistSong: PlaylistSong)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)
}


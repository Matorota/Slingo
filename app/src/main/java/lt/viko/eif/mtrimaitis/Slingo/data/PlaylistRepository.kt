package lt.viko.eif.mtrimaitis.Slingo.data

import kotlinx.coroutines.flow.Flow
import lt.viko.eif.mtrimaitis.Slingo.data.dao.PlaylistDao
import lt.viko.eif.mtrimaitis.Slingo.data.models.Playlist
import lt.viko.eif.mtrimaitis.Slingo.data.models.Song

class PlaylistRepository(private val playlistDao: PlaylistDao) {
    fun getPlaylistsByUser(userId: Long): Flow<List<Playlist>> {
        return playlistDao.getPlaylistsByUser(userId)
    }

    suspend fun createPlaylist(name: String, userId: Long): Result<Playlist> {
        return try {
            val playlist = Playlist(
                name = name,
                userId = userId
            )
            val playlistId = playlistDao.insertPlaylist(playlist)
            Result.success(playlist.copy(id = playlistId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: String) {
        val playlistSong = lt.viko.eif.mtrimaitis.Slingo.data.models.PlaylistSong(
            playlistId = playlistId,
            songId = songId
        )
        playlistDao.insertPlaylistSong(playlistSong)
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsInPlaylist(playlistId)
    }

    suspend fun getPlaylistById(id: Long): Playlist? {
        return playlistDao.getPlaylistById(id)
    }
}


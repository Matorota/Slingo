package lt.viko.eif.mtrimaitis.Slingo.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import lt.viko.eif.mtrimaitis.Slingo.data.dao.PlaylistDao
import lt.viko.eif.mtrimaitis.Slingo.data.dao.SharedPlaylistDao
import lt.viko.eif.mtrimaitis.Slingo.data.models.Playlist
import lt.viko.eif.mtrimaitis.Slingo.data.models.PlaylistSong
import lt.viko.eif.mtrimaitis.Slingo.data.models.SharedPlaylist

class SharedPlaylistRepository(
    private val sharedPlaylistDao: SharedPlaylistDao,
    private val playlistDao: PlaylistDao
) {
    fun getPendingSharedPlaylistsForUser(userId: Long): Flow<List<SharedPlaylist>> {
        return sharedPlaylistDao.getPendingSharedPlaylistsForUser(userId)
    }

    fun getAcceptedSharedPlaylistsForUser(userId: Long): Flow<List<SharedPlaylist>> {
        return sharedPlaylistDao.getAcceptedSharedPlaylistsForUser(userId)
    }

    suspend fun sharePlaylist(playlistId: Long, fromUserId: Long, toUserId: Long): Result<SharedPlaylist> {
        return try {
            // Check if already shared
            val existing = sharedPlaylistDao.getPendingSharedPlaylist(playlistId, toUserId)
            if (existing != null) {
                return Result.failure(Exception("Playlist already shared with this user"))
            }

            val sharedPlaylist = SharedPlaylist(
                playlistId = playlistId,
                fromUserId = fromUserId,
                toUserId = toUserId
            )
            val id = sharedPlaylistDao.insertSharedPlaylist(sharedPlaylist)
            Result.success(sharedPlaylist.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptSharedPlaylist(sharedPlaylistId: Long, acceptingUserId: Long): Result<Playlist> {
        return try {
            val sharedPlaylist = sharedPlaylistDao.getSharedPlaylistById(sharedPlaylistId)
                ?: return Result.failure(Exception("Shared playlist not found"))
            
            // Get the original playlist
            val originalPlaylist = playlistDao.getPlaylistById(sharedPlaylist.playlistId)
                ?: return Result.failure(Exception("Original playlist not found"))
            
            // Get all songs from the original playlist
            val originalSongs = playlistDao.getPlaylistSongs(sharedPlaylist.playlistId).first()
            
            // Create a new playlist for the accepting user with a name indicating it's shared
            val newPlaylistName = "${originalPlaylist.name} (Shared)"
            val newPlaylist = Playlist(
                name = newPlaylistName,
                userId = acceptingUserId
            )
            val newPlaylistId = playlistDao.insertPlaylist(newPlaylist)
            
            // Copy all songs to the new playlist
            originalSongs.forEach { playlistSong ->
                val newPlaylistSong = PlaylistSong(
                    playlistId = newPlaylistId,
                    songId = playlistSong.songId
                )
                playlistDao.insertPlaylistSong(newPlaylistSong)
            }
            
            // Update shared playlist status
            sharedPlaylistDao.updateSharedPlaylist(sharedPlaylist.copy(status = "accepted"))
            
            Result.success(newPlaylist.copy(id = newPlaylistId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun declineSharedPlaylist(sharedPlaylistId: Long) {
        try {
            val sharedPlaylist = sharedPlaylistDao.getSharedPlaylistById(sharedPlaylistId)
            if (sharedPlaylist != null) {
                sharedPlaylistDao.updateSharedPlaylist(sharedPlaylist.copy(status = "declined"))
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}


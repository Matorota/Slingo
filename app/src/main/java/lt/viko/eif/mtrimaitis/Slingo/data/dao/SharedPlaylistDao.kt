package lt.viko.eif.mtrimaitis.Slingo.data.dao

import androidx.room.*
import lt.viko.eif.mtrimaitis.Slingo.data.models.SharedPlaylist
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedPlaylistDao {
    @Query("SELECT * FROM shared_playlists WHERE toUserId = :userId AND status = 'pending'")
    fun getPendingSharedPlaylistsForUser(userId: Long): Flow<List<SharedPlaylist>>

    @Query("SELECT * FROM shared_playlists WHERE toUserId = :userId AND status = 'accepted'")
    fun getAcceptedSharedPlaylistsForUser(userId: Long): Flow<List<SharedPlaylist>>

    @Query("SELECT * FROM shared_playlists WHERE playlistId = :playlistId AND toUserId = :userId AND status = 'pending' LIMIT 1")
    suspend fun getPendingSharedPlaylist(playlistId: Long, userId: Long): SharedPlaylist?

    @Query("SELECT * FROM shared_playlists WHERE id = :id LIMIT 1")
    suspend fun getSharedPlaylistById(id: Long): SharedPlaylist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedPlaylist(sharedPlaylist: SharedPlaylist): Long

    @Update
    suspend fun updateSharedPlaylist(sharedPlaylist: SharedPlaylist)

    @Delete
    suspend fun deleteSharedPlaylist(sharedPlaylist: SharedPlaylist)
}


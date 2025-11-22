package lt.viko.eif.mtrimaitis.Slingo.data.dao

import androidx.room.*
import lt.viko.eif.mtrimaitis.Slingo.data.models.FriendRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendRequestDao {
    @Query("SELECT * FROM friend_requests WHERE toUserId = :userId AND status = 'pending'")
    fun getPendingRequestsForUser(userId: Long): Flow<List<FriendRequest>>

    @Query("SELECT * FROM friend_requests WHERE fromUserId = :userId AND status = 'pending'")
    fun getSentRequestsByUser(userId: Long): Flow<List<FriendRequest>>

    @Query("SELECT * FROM friend_requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: Long): FriendRequest?

    @Query("SELECT * FROM friend_requests WHERE fromUserId = :fromUserId AND toUserId = :toUserId AND status = 'pending' LIMIT 1")
    suspend fun getPendingRequest(fromUserId: Long, toUserId: Long): FriendRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: FriendRequest): Long

    @Update
    suspend fun updateRequest(request: FriendRequest)

    @Delete
    suspend fun deleteRequest(request: FriendRequest)

    @Query("DELETE FROM friend_requests WHERE fromUserId = :fromUserId AND toUserId = :toUserId")
    suspend fun deleteRequestByUsers(fromUserId: Long, toUserId: Long)
}


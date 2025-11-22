package lt.viko.eif.mtrimaitis.Slingo.data.dao

import androidx.room.*
import lt.viko.eif.mtrimaitis.Slingo.data.models.Friendship
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendshipDao {
    @Query("""
        SELECT * FROM friendships 
        WHERE userId1 = :userId OR userId2 = :userId
    """)
    fun getFriendshipsForUser(userId: Long): Flow<List<Friendship>>

    @Query("""
        SELECT * FROM friendships 
        WHERE (userId1 = :userId1 AND userId2 = :userId2) 
           OR (userId1 = :userId2 AND userId2 = :userId1)
        LIMIT 1
    """)
    suspend fun getFriendship(userId1: Long, userId2: Long): Friendship?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriendship(friendship: Friendship): Long

    @Delete
    suspend fun deleteFriendship(friendship: Friendship)
}


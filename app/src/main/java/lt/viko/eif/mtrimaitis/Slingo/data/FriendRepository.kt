package lt.viko.eif.mtrimaitis.Slingo.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import lt.viko.eif.mtrimaitis.Slingo.data.dao.FriendRequestDao
import lt.viko.eif.mtrimaitis.Slingo.data.dao.FriendshipDao
import lt.viko.eif.mtrimaitis.Slingo.data.dao.UserDao
import lt.viko.eif.mtrimaitis.Slingo.data.models.FriendRequest
import lt.viko.eif.mtrimaitis.Slingo.data.models.Friendship
import lt.viko.eif.mtrimaitis.Slingo.data.models.User

class FriendRepository(
    private val userDao: UserDao,
    private val friendRequestDao: FriendRequestDao,
    private val friendshipDao: FriendshipDao
) {
    suspend fun searchUsers(query: String, excludeUserId: Long): List<User> {
        return if (query.isBlank()) {
            userDao.getAllUsersExcept(excludeUserId)
        } else {
            userDao.searchUsers("%$query%").filter { it.id != excludeUserId }
        }
    }

    fun getPendingRequestsForUser(userId: Long): Flow<List<FriendRequest>> {
        return friendRequestDao.getPendingRequestsForUser(userId)
    }

    fun getSentRequestsByUser(userId: Long): Flow<List<FriendRequest>> {
        return friendRequestDao.getSentRequestsByUser(userId)
    }

    suspend fun sendFriendRequest(fromUserId: Long, toUserId: Long): Result<FriendRequest> {
        return try {
            // Check if already friends
            val existingFriendship = friendshipDao.getFriendship(fromUserId, toUserId)
            if (existingFriendship != null) {
                return Result.failure(Exception("Already friends"))
            }

            // Check if request already exists
            val existingRequest = friendRequestDao.getPendingRequest(fromUserId, toUserId)
            if (existingRequest != null) {
                return Result.failure(Exception("Request already sent"))
            }

            val request = FriendRequest(
                fromUserId = fromUserId,
                toUserId = toUserId
            )
            val requestId = friendRequestDao.insertRequest(request)
            Result.success(request.copy(id = requestId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptFriendRequest(requestId: Long): Result<Friendship> {
        return try {
            val request = friendRequestDao.getRequestById(requestId)
                ?: return Result.failure(Exception("Request not found"))

            // Create friendship
            val friendship = Friendship(
                userId1 = request.fromUserId,
                userId2 = request.toUserId
            )
            val friendshipId = friendshipDao.insertFriendship(friendship)

            // Update request status
            friendRequestDao.updateRequest(request.copy(status = "accepted"))

            Result.success(friendship.copy(id = friendshipId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun declineFriendRequest(requestId: Long) {
        try {
            val request = friendRequestDao.getRequestById(requestId)
            if (request != null) {
                friendRequestDao.updateRequest(request.copy(status = "declined"))
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun getFriendshipsForUser(userId: Long): Flow<List<Friendship>> {
        return friendshipDao.getFriendshipsForUser(userId)
    }

    suspend fun getFriendUserIds(userId: Long): List<Long> {
        val friendships = friendshipDao.getFriendshipsForUser(userId).first()
        return friendships.map { friendship ->
            if (friendship.userId1 == userId) friendship.userId2 else friendship.userId1
        }
    }

    suspend fun removeFriend(userId: Long, friendId: Long) {
        val friendship = friendshipDao.getFriendship(userId, friendId)
        if (friendship != null) {
            friendshipDao.deleteFriendship(friendship)
        }
    }
}


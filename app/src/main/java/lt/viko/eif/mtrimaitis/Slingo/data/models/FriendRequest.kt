package lt.viko.eif.mtrimaitis.Slingo.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "friend_requests",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["fromUserId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["toUserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["fromUserId"]),
        Index(value = ["toUserId"]),
        Index(value = ["fromUserId", "toUserId"], unique = true)
    ]
)
data class FriendRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fromUserId: Long,
    val toUserId: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "pending" // pending, accepted, declined
)


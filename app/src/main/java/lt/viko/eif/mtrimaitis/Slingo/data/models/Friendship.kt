package lt.viko.eif.mtrimaitis.Slingo.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "friendships",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId1"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId2"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId1"]),
        Index(value = ["userId2"]),
        Index(value = ["userId1", "userId2"], unique = true)
    ]
)
data class Friendship(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId1: Long,
    val userId2: Long,
    val createdAt: Long = System.currentTimeMillis()
)


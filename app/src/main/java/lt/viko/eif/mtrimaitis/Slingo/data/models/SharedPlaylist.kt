package lt.viko.eif.mtrimaitis.Slingo.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shared_playlists",
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
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
        Index(value = ["playlistId"]),
        Index(value = ["fromUserId"]),
        Index(value = ["toUserId"]),
        Index(value = ["playlistId", "toUserId"], unique = true)
    ]
)
data class SharedPlaylist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistId: Long,
    val fromUserId: Long,
    val toUserId: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "pending" // pending, accepted, declined
)


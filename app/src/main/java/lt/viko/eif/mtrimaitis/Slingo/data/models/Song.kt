package lt.viko.eif.mtrimaitis.Slingo.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey
    val id: String,
    val name: String,
    val artist: String,
    val album: String = "",
    val imageUrl: String = "",
    val previewUrl: String = "",
    val duration: Int = 0,
    val spotifyUri: String = ""
)


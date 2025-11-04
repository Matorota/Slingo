package lt.viko.eif.mtrimaitis.Slingo.data.models

import com.google.gson.annotations.SerializedName

data class SpotifySearchResponse(
    @SerializedName("tracks")
    val tracks: TracksResponse
)

data class TracksResponse(
    @SerializedName("items")
    val items: List<SpotifyTrack>
)

data class SpotifyTrack(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("artists")
    val artists: List<SpotifyArtist>,
    @SerializedName("album")
    val album: SpotifyAlbum,
    @SerializedName("preview_url")
    val previewUrl: String?,
    @SerializedName("duration_ms")
    val durationMs: Int,
    @SerializedName("uri")
    val uri: String
)

data class SpotifyArtist(
    @SerializedName("name")
    val name: String
)

data class SpotifyAlbum(
    @SerializedName("name")
    val name: String,
    @SerializedName("images")
    val images: List<SpotifyImage>
)

data class SpotifyImage(
    @SerializedName("url")
    val url: String
)

data class SpotifyTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("expires_in")
    val expiresIn: Int
)


package lt.viko.eif.mtrimaitis.Slingo.data.models

import com.google.gson.annotations.SerializedName

data class YouTubeSearchResponse(
    @SerializedName("items")
    val items: List<YouTubeItem>
)

data class YouTubeItem(
    @SerializedName("id")
    val id: Any?, // Can be YouTubeId (from search) or String (from video details)
    @SerializedName("snippet")
    val snippet: YouTubeSnippet?,
    @SerializedName("contentDetails")
    val contentDetails: YouTubeContentDetails?
) {
    // Helper to get video ID from either format
    fun getVideoId(): String? {
        return when (id) {
            is YouTubeId -> id.videoId
            is String -> id
            else -> null
        }
    }
}

data class YouTubeId(
    @SerializedName("videoId")
    val videoId: String?,
    @SerializedName("kind")
    val kind: String?
)

data class YouTubeSnippet(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("channelTitle")
    val channelTitle: String?,
    @SerializedName("thumbnails")
    val thumbnails: YouTubeThumbnails?
)

data class YouTubeThumbnails(
    @SerializedName("default")
    val default: YouTubeThumbnail?,
    @SerializedName("medium")
    val medium: YouTubeThumbnail?,
    @SerializedName("high")
    val high: YouTubeThumbnail?
)

data class YouTubeThumbnail(
    @SerializedName("url")
    val url: String
)

data class YouTubeContentDetails(
    @SerializedName("duration")
    val duration: String? // ISO 8601 duration format (e.g., "PT3M30S")
)


package lt.viko.eif.mtrimaitis.Slingo.data

import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import lt.viko.eif.mtrimaitis.Slingo.data.api.RetrofitClient
import lt.viko.eif.mtrimaitis.Slingo.data.api.SpotifyApi
import lt.viko.eif.mtrimaitis.Slingo.data.api.YouTubeApi
import lt.viko.eif.mtrimaitis.Slingo.data.dao.SongDao
import lt.viko.eif.mtrimaitis.Slingo.data.models.Song
import lt.viko.eif.mtrimaitis.Slingo.data.models.SpotifyTrack
import lt.viko.eif.mtrimaitis.Slingo.data.models.YouTubeItem
import java.util.regex.Pattern

class SongRepository(
    private val songDao: SongDao,
    private val spotifyApi: SpotifyApi = RetrofitClient.spotifyApi,
    private val youtubeApi: YouTubeApi = RetrofitClient.youtubeApi
) {
    private var accessToken: String? = null
    private var tokenExpiryTime: Long = 0
    private val recommendationSeeds = listOf(
        "today's top hits",
        "new music friday",
        "chill vibes",
        "edm workout",
        "indie essentials",
        "jazz classics",
        "throwback pop",
        "soothing piano",
        "lofi beats",
        "rock anthems"
    )

    suspend fun getAccessToken(): String? {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return accessToken
        }

        return try {
            val auth = RetrofitClient.getBasicAuthHeader()

            val response = spotifyApi.getAccessToken(
                grantType = "client_credentials",
                authorization = auth
            )

            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                accessToken = tokenResponse.accessToken
                tokenExpiryTime = System.currentTimeMillis() + (tokenResponse.expiresIn * 1000L)
                accessToken
            } else {
                Log.e("SongRepository", "Failed to get access token: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Error fetching access token", e)
            null
        }
    }

    suspend fun searchTracks(query: String): Result<List<Song>> {
        val sanitizedQuery = query.trim()
        if (sanitizedQuery.isEmpty()) {
            return Result.success(emptyList())
        }

        return try {
            val token = getAccessToken()
            if (token == null) {
                return Result.failure(Exception("Failed to get access token"))
            }

            val markets = listOf("US", "GB")
            val collected = mutableListOf<Song>()
            var lastException: Exception? = null

            for (market in markets) {
                try {
                    val response = spotifyApi.searchTracks(
                        query = sanitizedQuery,
                        type = "track",
                        limit = 50,
                        market = market,
                        authorization = "Bearer $token"
                    )

                    if (response.isSuccessful && response.body() != null) {
                        val rawItems = response.body()!!.tracks.items

                        // Convert Spotify tracks to Songs
                        val songs = rawItems.map { spotifyTrack ->
                            val enrichedTrack = ensurePreviewAvailability(spotifyTrack, token)
                            convertSpotifyTrackToSong(enrichedTrack)
                        }

                        // Enrich with YouTube video IDs for full playback (async)
                        android.util.Log.d("SongRepository", "Enriching ${songs.size} songs with YouTube video IDs...")
                        val enrichedSongs = mutableListOf<Song>()
                        for (song in songs) {
                            val enriched = enrichSongWithYouTube(song)
                            enrichedSongs.add(enriched)
                            if (enriched.youtubeVideoId.isNotEmpty()) {
                                android.util.Log.d("SongRepository", "✅ Enriched: ${enriched.name} - YouTube ID: ${enriched.youtubeVideoId}")
                            } else {
                                android.util.Log.d("SongRepository", "⚠️ No YouTube ID for: ${enriched.name} (will use Spotify preview)")
                            }
                        }
                        android.util.Log.d("SongRepository", "Enrichment complete. ${enrichedSongs.count { it.youtubeVideoId.isNotEmpty() }} songs have YouTube IDs")

                        collected += enrichedSongs

                        if (collected.isNotEmpty()) {
                            break
                        }
                    } else {
                        lastException = Exception("API call failed: ${response.code()}")
                    }
                } catch (e: Exception) {
                    lastException = e
                }
            }

            val uniqueSongs = collected.distinctBy { it.id }
            if (uniqueSongs.isEmpty()) {
                if (lastException != null) {
                    Result.failure(lastException as Exception)
                } else {
                    Result.success(emptyList())
                }
            } else {
                songDao.insertSongs(uniqueSongs)
                Result.success(uniqueSongs)
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Error searching tracks", e)
            Result.failure(e)
        }
    }

    private suspend fun ensurePreviewAvailability(track: SpotifyTrack, token: String): SpotifyTrack {
        if (!track.previewUrl.isNullOrBlank()) {
            return track
        }

        val marketsToTry = listOf("US", "GB", "SE", "DE", "FR", "CA", "AU", "JP", "BR")

        marketsToTry.forEach { market ->
            try {
                val detailResponse = spotifyApi.getTrack(
                    id = track.id,
                    market = market,
                    authorization = "Bearer $token"
                )
                if (detailResponse.isSuccessful) {
                    val detailedTrack = detailResponse.body()
                    if (!detailedTrack?.previewUrl.isNullOrBlank()) {
                        return detailedTrack!!
                    }
                }
            } catch (e: Exception) {
                Log.e("SongRepository", "Error fetching track details for market $market", e)
            }
        }

        return track
    }

    private fun convertSpotifyTrackToSong(track: SpotifyTrack): Song {
        val artistName = track.artists.joinToString(", ") { it.name }
        val imageUrl = track.album.images.firstOrNull()?.url ?: ""

        return Song(
            id = track.id,
            name = track.name,
            artist = artistName,
            album = track.album.name,
            imageUrl = imageUrl,
            previewUrl = track.previewUrl ?: "", // Keep Spotify preview as fallback
            duration = track.durationMs / 1000,
            spotifyUri = track.uri,
            youtubeVideoId = "", // Will be filled by enrichSongWithYouTube
            source = "spotify"
        )
    }

    /**
     * Enriches a Spotify song with YouTube video ID for full playback.
     * Searches YouTube for the same track and stores the video ID.
     */
    private suspend fun enrichSongWithYouTube(song: Song): Song {
        if (song.youtubeVideoId.isNotEmpty()) {
            return song // Already has YouTube ID
        }

        // Try direct YouTube API first
        val directResult = tryEnrichWithYouTubeAPI(song)
        if (directResult.youtubeVideoId.isNotEmpty()) {
            return directResult
        }

        // Fallback: Try backend YouTube search
        Log.d("SongRepository", "Direct YouTube API failed, trying backend search for: ${song.name}")
        return tryEnrichWithBackendSearch(song)
    }

    /**
     * Try to enrich using YouTube Data API directly.
     */
    private suspend fun tryEnrichWithYouTubeAPI(song: Song): Song {
        return try {
            val apiKey = RetrofitClient.getYouTubeApiKey()
            if (apiKey == "YOUR_YOUTUBE_API_KEY_HERE" || apiKey.isEmpty()) {
                Log.w("SongRepository", "YouTube API key not configured, skipping YouTube enrichment")
                return song
            }

            // Build search query: "Artist - Song Name"
            val searchQuery = "${song.artist} ${song.name} official"
            Log.d("SongRepository", "Searching YouTube API for: $searchQuery")
            
            val response = youtubeApi.searchVideos(
                query = searchQuery,
                maxResults = 1, // Only need the first result
                apiKey = apiKey
            )

            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.items
                val videoId = items.firstOrNull()?.getVideoId()
                
                if (videoId != null && isValidYouTubeVideoId(videoId)) {
                    Log.d("SongRepository", "✅ Found YouTube video via API for: ${song.name} - ${song.artist} (ID: $videoId)")
                    song.copy(youtubeVideoId = videoId)
                } else {
                    if (videoId != null) {
                        Log.w("SongRepository", "Invalid YouTube video ID format: $videoId (expected 11 characters)")
                    } else {
                        Log.w("SongRepository", "YouTube API returned no results for: ${song.name}")
                    }
                    song
                }
            } else {
                Log.w("SongRepository", "YouTube API search failed for: ${song.name} - Code: ${response.code()}, Message: ${response.message()}")
                song
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Error in YouTube API search for: ${song.name}", e)
            song
        }
    }

    /**
     * Try to enrich using backend YouTube search endpoint.
     * Backend endpoint: GET /search?q={query}
     */
    private suspend fun tryEnrichWithBackendSearch(song: Song): Song {
        return try {
            // Backend URL for YouTube search - auto-detect connection type
            val BACKEND_URL = getBackendUrl(3000)
            
            val searchQuery = "${song.artist} ${song.name} official"
            val encodedQuery = java.net.URLEncoder.encode(searchQuery, "UTF-8")
            // Try search-simple endpoint first (as shown in backend output), fallback to search
            val backendUrl = "$BACKEND_URL/api/youtube/search-simple?q=$encodedQuery&maxResults=1"
            
            Log.d("SongRepository", "Searching backend YouTube API: $backendUrl")
            
            // Execute network call on IO dispatcher to avoid NetworkOnMainThreadException
            val response = withContext(Dispatchers.IO) {
                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url(backendUrl)
                    .header("User-Agent", "Slingo-Android-App")
                    .build()
                
                client.newCall(request).execute()
            }
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    Log.d("SongRepository", "Backend search response: ${responseBody.take(200)}...") // Log first 200 chars
                    val json = org.json.JSONObject(responseBody)
                    
                    // Try search-simple format first (returns {success: true, videoId: "..."})
                    if (json.optBoolean("success", false)) {
                        val videoId = json.optString("videoId", "")
                        if (videoId.isNotEmpty() && isValidYouTubeVideoId(videoId)) {
                            Log.d("SongRepository", "✅ Found YouTube video via backend (search-simple) for: ${song.name} (ID: $videoId)")
                            return song.copy(youtubeVideoId = videoId)
                        }
                    }
                    
                    // Try full search format (returns {success: true, data: {items: [...]}})
                    if (json.optBoolean("success", false)) {
                        val data = json.optJSONObject("data")
                        val items = data?.optJSONArray("items")
                        if (items != null && items.length() > 0) {
                            val firstItem = items.getJSONObject(0)
                            val videoIdObj = firstItem.optJSONObject("id")
                            val videoId = videoIdObj?.optString("videoId")
                            
                            if (videoId != null && videoId.isNotEmpty() && isValidYouTubeVideoId(videoId)) {
                                Log.d("SongRepository", "✅ Found YouTube video via backend for: ${song.name} (ID: $videoId)")
                                return song.copy(youtubeVideoId = videoId)
                            } else if (videoId != null && !isValidYouTubeVideoId(videoId)) {
                                Log.w("SongRepository", "Invalid YouTube video ID from backend: $videoId (expected 11 characters)")
                            }
                        }
                    }
                }
            } else {
                val errorBody = response.body?.string()
                Log.w("SongRepository", "Backend YouTube search failed for: ${song.name} - Code: ${response.code}, Error: ${errorBody?.take(200)}")
            }
            song
        } catch (e: Exception) {
            Log.e("SongRepository", "Error in backend YouTube search for: ${song.name}", e)
            song
        }
    }

    /**
     * Search for music videos on YouTube.
     * YouTube provides full-length tracks, unlike Spotify's 30-second previews.
     */
    suspend fun searchYouTubeTracks(query: String): Result<List<Song>> {
        val sanitizedQuery = query.trim()
        if (sanitizedQuery.isEmpty()) {
            return Result.success(emptyList())
        }

        return try {
            val apiKey = RetrofitClient.getYouTubeApiKey()
            if (apiKey == "YOUR_YOUTUBE_API_KEY_HERE") {
                return Result.failure(Exception("YouTube API key not configured. Please add your API key in RetrofitClient.kt"))
            }

            // Search for videos - add "music" to query for better results
            val searchQuery = "$sanitizedQuery music"
            val response = youtubeApi.searchVideos(
                query = searchQuery,
                maxResults = 20,
                apiKey = apiKey
            )

            if (response.isSuccessful && response.body() != null) {
                val items = response.body()!!.items
                val songs = items.mapNotNull { item ->
                    val videoId = item.getVideoId()
                    if (videoId != null) {
                        convertYouTubeItemToSong(item, videoId)
                    } else null
                }

                if (songs.isNotEmpty()) {
                    songDao.insertSongs(songs)
                    Result.success(songs)
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception("YouTube API call failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Error searching YouTube tracks", e)
            Result.failure(e)
        }
    }

    /**
     * Convert YouTube video item to Song model.
     * Extracts artist and title from video title (format: "Artist - Title" or "Title - Artist")
     */
    private fun convertYouTubeItemToSong(item: YouTubeItem, videoId: String): Song {
        val snippet = item.snippet ?: return Song(
            id = videoId,
            name = "Unknown",
            artist = "Unknown",
            source = "youtube",
            youtubeVideoId = videoId
        )

        val title = snippet.title
        val thumbnail = snippet.thumbnails?.high?.url 
            ?: snippet.thumbnails?.medium?.url 
            ?: snippet.thumbnails?.default?.url 
            ?: ""

        // Try to parse "Artist - Title" or "Title - Artist" format
        val titleParts = title.split(" - ", " – ", " | ", limit = 2)
        val (artist, songName) = if (titleParts.size == 2) {
            // Check which format: usually "Artist - Title"
            val first = titleParts[0].trim()
            val second = titleParts[1].trim()
            // Heuristic: if second part is shorter or contains common video suffixes, it's likely the title
            if (second.length < first.length || second.contains("(Official", ignoreCase = true)) {
                first to second.replace(Regex("\\(.*\\)|\\[.*\\]|Official.*", RegexOption.IGNORE_CASE), "").trim()
            } else {
                second to first.replace(Regex("\\(.*\\)|\\[.*\\]|Official.*", RegexOption.IGNORE_CASE), "").trim()
            }
        } else {
            // Can't parse, use channel as artist
            (snippet.channelTitle ?: "Unknown") to title.replace(Regex("\\(.*\\)|\\[.*\\]|Official.*", RegexOption.IGNORE_CASE), "").trim()
        }

        // Parse duration from ISO 8601 format (PT3M30S = 3 minutes 30 seconds)
        val duration = parseYouTubeDuration(item.contentDetails?.duration)

        // Get audio stream URL (this will need proper implementation with extraction library)
        val audioUrl = "https://www.youtube.com/watch?v=$videoId" // Placeholder

        return Song(
            id = "yt_$videoId", // Prefix to avoid conflicts with Spotify IDs
            name = songName.ifEmpty { title },
            artist = artist,
            album = "",
            imageUrl = thumbnail,
            previewUrl = audioUrl, // This should be the actual audio stream URL
            duration = duration,
            spotifyUri = "",
            youtubeVideoId = videoId,
            source = "youtube"
        )
    }

    /**
     * Parse YouTube duration from ISO 8601 format (e.g., "PT3M30S" = 210 seconds)
     */
    private fun parseYouTubeDuration(duration: String?): Int {
        if (duration == null) return 0
        
        val pattern = Pattern.compile("PT(?:([0-9]+)H)?(?:([0-9]+)M)?(?:([0-9]+)S)?")
        val matcher = pattern.matcher(duration)
        
        if (matcher.find()) {
            val hours = (matcher.group(1)?.toIntOrNull() ?: 0)
            val minutes = (matcher.group(2)?.toIntOrNull() ?: 0)
            val seconds = (matcher.group(3)?.toIntOrNull() ?: 0)
            return hours * 3600 + minutes * 60 + seconds
        }
        return 0
    }

    /**
     * Combined search: Try Spotify first, then YouTube if Spotify fails or returns no results.
     * YouTube results provide full-length tracks.
     */
    suspend fun searchTracksCombined(query: String, preferYouTube: Boolean = false): Result<List<Song>> {
        return if (preferYouTube) {
            // Try YouTube first for full tracks
            searchYouTubeTracks(query).onSuccess { youtubeSongs ->
                if (youtubeSongs.isNotEmpty()) {
                    return Result.success(youtubeSongs)
                }
            }
            // Fallback to Spotify
            searchTracks(query)
        } else {
            // Try Spotify first
            searchTracks(query).onSuccess { spotifySongs ->
                if (spotifySongs.isNotEmpty()) {
                    return Result.success(spotifySongs)
                }
            }
            // Fallback to YouTube for full tracks
            searchYouTubeTracks(query)
        }
    }

    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()

    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs("%$query%")

    suspend fun getSongById(id: String): Song? = songDao.getSongById(id)

    suspend fun insertSong(song: Song) = songDao.insertSong(song)

    suspend fun getRecommendedSongs(limit: Int = 12): Result<List<Song>> {
        return try {
            val collected = songDao.getRandomSongs(limit).toMutableList()

            if (collected.size < limit) {
                val seeds = recommendationSeeds.shuffled()
                for (seed in seeds) {
                    searchTracks(seed).onSuccess { songs ->
                        songs.forEach { song ->
                            if (collected.none { it.id == song.id }) {
                                collected.add(song)
                            }
                        }
                    }
                    if (collected.size >= limit) break
                }
            }

            if (collected.isEmpty()) {
                Result.failure(Exception("No recommendations available right now."))
            } else {
                Result.success(collected.distinctBy { it.id }.take(limit))
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Error retrieving recommendations", e)
            Result.failure(e)
        }
    }

    /**
     * Enriches a song on-demand if it's missing YouTube ID or preview URL.
     * Used when playing songs from favorites/library that weren't enriched during search.
     */
    suspend fun enrichSongIfNeeded(song: Song): Song {
        // If song already has both, return as-is
        if (song.youtubeVideoId.isNotEmpty() && song.previewUrl.isNotEmpty()) {
            return song
        }

        var enriched = song

        // Try to get YouTube ID if missing
        if (enriched.youtubeVideoId.isEmpty()) {
            Log.d("SongRepository", "Enriching song with YouTube ID: ${song.name}")
            enriched = enrichSongWithYouTube(enriched)
        }

        // If still no preview URL, try to get it from Spotify (try multiple markets)
        if (enriched.previewUrl.isEmpty() && enriched.spotifyUri.isNotEmpty()) {
            Log.d("SongRepository", "Trying to get Spotify preview for: ${song.name}")
            try {
                val token = getAccessToken()
                if (token != null) {
                    // Extract Spotify track ID from URI (format: spotify:track:ID)
                    val trackId = enriched.spotifyUri.replace("spotify:track:", "")
                    if (trackId.isNotEmpty()) {
                        // Try multiple markets to find preview
                        val markets = listOf("US", "GB", "SE", "DE", "FR", "CA", "AU", "JP", "BR", "NL", "NO", "DK")
                        for (market in markets) {
                            try {
                                val response = spotifyApi.getTrack(
                                    id = trackId,
                                    market = market,
                                    authorization = "Bearer $token"
                                )
                                if (response.isSuccessful && response.body() != null) {
                                    val track = response.body()!!
                                    if (!track.previewUrl.isNullOrBlank()) {
                                        enriched = enriched.copy(previewUrl = track.previewUrl)
                                        Log.d("SongRepository", "✅ Got Spotify preview URL for: ${song.name} (market: $market)")
                                        break
                                    }
                                }
                            } catch (e: Exception) {
                                // Try next market
                                Log.d("SongRepository", "Market $market failed: ${e.message}")
                            }
                        }
                        if (enriched.previewUrl.isEmpty()) {
                            Log.w("SongRepository", "⚠️ No Spotify preview available for: ${song.name} (tried ${markets.size} markets)")
                        }
                    } else {
                        Log.w("SongRepository", "Invalid Spotify URI: ${enriched.spotifyUri}")
                    }
                } else {
                    Log.w("SongRepository", "Failed to get Spotify access token")
                }
            } catch (e: Exception) {
                Log.e("SongRepository", "Error getting Spotify preview: ${e.message}", e)
            }
        }

        // Update in database if we got new data
        if (enriched.youtubeVideoId != song.youtubeVideoId || enriched.previewUrl != song.previewUrl) {
            try {
                songDao.insertSong(enriched)
                Log.d("SongRepository", "Updated song in database: ${enriched.name}")
            } catch (e: Exception) {
                Log.w("SongRepository", "Failed to update song in database: ${e.message}")
            }
        }

        return enriched
    }
    
    /**
     * Detects if app is running on Android emulator.
     */
    private fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }
    
    /**
     * Gets the appropriate backend URL based on connection type.
     * Priority: USB port forwarding (localhost) > Emulator (10.0.2.2) > WiFi IP (10.77.146.73)
     */
    private fun getBackendUrl(port: Int): String {
        // For emulator, use 10.0.2.2 which maps to localhost
        if (isRunningOnEmulator()) {
            return "http://10.0.2.2:$port"
        }
        
        // For physical device:
        // - If USB connected with ADB port forwarding: use localhost (127.0.0.1)
        // - If WiFi only: use computer's IP (10.77.146.73)
        // Currently using WiFi IP - user can set up ADB port forwarding for localhost
        return "http://10.77.146.73:$port"
        
        // To use USB port forwarding, uncomment below and set up ADB forwarding:
        // return "http://127.0.0.1:$port"  // Requires: adb reverse tcp:$port tcp:$port
    }
    
    /**
     * Validates if a string is a valid YouTube video ID.
     * YouTube video IDs are exactly 11 characters and contain alphanumeric characters, hyphens, and underscores.
     */
    private fun isValidYouTubeVideoId(videoId: String): Boolean {
        if (videoId.length != 11) {
            return false
        }
        // YouTube video IDs contain: A-Z, a-z, 0-9, -, _
        val pattern = Pattern.compile("^[a-zA-Z0-9_-]{11}$")
        return pattern.matcher(videoId).matches()
    }
}

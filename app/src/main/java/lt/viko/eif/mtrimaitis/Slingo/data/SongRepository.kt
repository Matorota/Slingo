package lt.viko.eif.mtrimaitis.Slingo.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import lt.viko.eif.mtrimaitis.Slingo.data.api.RetrofitClient
import lt.viko.eif.mtrimaitis.Slingo.data.api.SpotifyApi
import lt.viko.eif.mtrimaitis.Slingo.data.dao.SongDao
import lt.viko.eif.mtrimaitis.Slingo.data.models.Song
import lt.viko.eif.mtrimaitis.Slingo.data.models.SpotifyTrack

class SongRepository(
    private val songDao: SongDao,
    private val spotifyApi: SpotifyApi = RetrofitClient.spotifyApi
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

                        val songs = rawItems.map { spotifyTrack ->
                            val enrichedTrack = ensurePreviewAvailability(spotifyTrack, token)
                            convertSpotifyTrackToSong(enrichedTrack)
                        }

                        collected += songs

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
            previewUrl = track.previewUrl ?: "",
            duration = track.durationMs / 1000,
            spotifyUri = track.uri
        )
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
}

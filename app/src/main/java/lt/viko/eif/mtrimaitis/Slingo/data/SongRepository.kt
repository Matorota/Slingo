package lt.viko.eif.mtrimaitis.Slingo.data

import android.util.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    suspend fun getAccessToken(): String? {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return accessToken
        }

        return try {
            // Use the proper Basic Auth header with client_id:client_secret
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
                // If authentication fails, return null but don't throw
                // The app will show an error message to the user
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun searchTracks(query: String): Result<List<Song>> {
        return try {
            val token = getAccessToken()
            if (token == null) {
                return Result.failure(Exception("Failed to get access token"))
            }

            // Increase limit for better search results
            val response = spotifyApi.searchTracks(
                query = query,
                type = "track",
                limit = 50,
                authorization = "Bearer $token"
            )

            if (response.isSuccessful && response.body() != null) {
                val tracks = response.body()!!.tracks.items.map { spotifyTrack ->
                    convertSpotifyTrackToSong(spotifyTrack)
                }
                
                // Save to local database
                songDao.insertSongs(tracks)
                
                Result.success(tracks)
            } else {
                Result.failure(Exception("API call failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
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
}

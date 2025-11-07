package lt.viko.eif.mtrimaitis.Slingo.data.api

import lt.viko.eif.mtrimaitis.Slingo.data.models.SpotifySearchResponse
import lt.viko.eif.mtrimaitis.Slingo.data.models.SpotifyTokenResponse
import retrofit2.Response
import retrofit2.http.*

interface SpotifyApi {
    @FormUrlEncoded
    @POST("https://accounts.spotify.com/api/token")
    suspend fun getAccessToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Header("Authorization") authorization: String
    ): Response<SpotifyTokenResponse>

    @GET("https://api.spotify.com/v1/search")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("limit") limit: Int = 20,
        @Query("market") market: String? = null,
        @Header("Authorization") authorization: String
    ): Response<SpotifySearchResponse>

    @GET("https://api.spotify.com/v1/tracks/{id}")
    suspend fun getTrack(
        @Path("id") id: String,
        @Query("market") market: String? = null,
        @Header("Authorization") authorization: String
    ): Response<lt.viko.eif.mtrimaitis.Slingo.data.models.SpotifyTrack>
}


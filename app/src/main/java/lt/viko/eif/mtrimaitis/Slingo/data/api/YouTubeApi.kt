package lt.viko.eif.mtrimaitis.Slingo.data.api

import lt.viko.eif.mtrimaitis.Slingo.data.models.YouTubeSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApi {
    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") apiKey: String
    ): Response<YouTubeSearchResponse>
    
    @GET("videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "contentDetails,snippet",
        @Query("id") videoId: String,
        @Query("key") apiKey: String
    ): Response<YouTubeSearchResponse>
}


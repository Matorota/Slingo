package lt.viko.eif.mtrimaitis.Slingo.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // fill the 3 api keys SPOTIFY_CLIENT_ID, SPOTIFY_CLIENT_SECRET, YOUTUBE_API_KEY


    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val spotifyApi: SpotifyApi = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SpotifyApi::class.java)

    val youtubeApi: YouTubeApi = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/youtube/v3/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(YouTubeApi::class.java)

    fun getSpotifyClientId(): String = SPOTIFY_CLIENT_ID

    fun getSpotifyClientSecret(): String = SPOTIFY_CLIENT_SECRET
    
    fun getYouTubeApiKey(): String = YOUTUBE_API_KEY
    
    fun getBasicAuthHeader(): String {
        val credentials = "$SPOTIFY_CLIENT_ID:$SPOTIFY_CLIENT_SECRET"
        return "Basic ${android.util.Base64.encodeToString(credentials.toByteArray(), android.util.Base64.NO_WRAP)}"
    }
}


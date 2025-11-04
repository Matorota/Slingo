package lt.viko.eif.mtrimaitis.Slingo.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val SPOTIFY_CLIENT_ID = "dcea1cbdd55d4c46b738d00caddac5b5"
    // TODO: Replace with your actual Client Secret from Spotify Dashboard
    // Click "View client secret" in your Spotify app settings to get this
    private const val SPOTIFY_CLIENT_SECRET = "656e6c7ec8fc4d25ae4cc783e5154cd5"
    
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

    fun getSpotifyClientId(): String = SPOTIFY_CLIENT_ID
    
    fun getSpotifyClientSecret(): String = SPOTIFY_CLIENT_SECRET
    
    fun getBasicAuthHeader(): String {
        val credentials = "$SPOTIFY_CLIENT_ID:$SPOTIFY_CLIENT_SECRET"
        return "Basic ${android.util.Base64.encodeToString(credentials.toByteArray(), android.util.Base64.NO_WRAP)}"
    }
}


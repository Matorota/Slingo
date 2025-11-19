package lt.viko.eif.mtrimaitis.Slingo.data

import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLDecoder
import java.util.regex.Pattern

/**
 * Utility class to extract audio stream URLs from YouTube videos.
 * 
 * This implementation uses YouTube's player API to get stream URLs.
 * Note: This is a simplified approach. For production, consider using
 * a backend service with yt-dlp or similar tools.
 */
object YouTubeAudioExtractor {
    
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .build()
    
    /**
     * Extracts audio stream URL from YouTube video ID.
     * 
     * Tries backend service first, then falls back to direct YouTube extraction.
     * 
     * @param videoId YouTube video ID
     * @return Audio stream URL or null if extraction fails
     */
    suspend fun getAudioStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d("YouTubeAudioExtractor", "Attempting to extract audio for video: $videoId")
            
            // Try backend service first (most reliable)
            val backendUrl = tryBackendService(videoId)
            if (backendUrl != null) {
                Log.d("YouTubeAudioExtractor", "Successfully got audio URL from backend")
                return@withContext backendUrl
            }
            
            Log.w("YouTubeAudioExtractor", "Backend service failed, trying direct YouTube extraction")
            
            // Fallback: Try direct YouTube extraction (may not work due to restrictions)
            val directUrl = tryDirectYouTubeExtraction(videoId)
            if (directUrl != null) {
                Log.d("YouTubeAudioExtractor", "Successfully extracted audio URL directly")
                return@withContext directUrl
            }
            
            Log.e("YouTubeAudioExtractor", "All extraction methods failed for video: $videoId")
            null
            
        } catch (e: Exception) {
            Log.e("YouTubeAudioExtractor", "Error extracting audio URL for video: $videoId", e)
            null
        }
    }
    
    /**
     * Tries to get audio URL from backend service.
     * Backend endpoint: GET /api/youtube/audio/:videoId
     */
    private suspend fun tryBackendService(videoId: String): String? {
        return try {
            // Backend URL - auto-detect connection type
            // Priority: USB port forwarding > Emulator > WiFi IP
            val BACKEND_URL = getBackendUrl(5000)
            
            Log.d("YouTubeAudioExtractor", "Using backend URL: $BACKEND_URL")
            
            // Backend endpoint matches your server: /api/youtube/audio/:videoId
            val backendUrl = "$BACKEND_URL/api/youtube/audio/$videoId"
            
            Log.d("YouTubeAudioExtractor", "Calling backend service: $backendUrl")
            
            val request = Request.Builder()
                .url(backendUrl)
                .header("User-Agent", "Slingo-Android-App")
                .header("Accept", "application/json")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val json = JSONObject(responseBody)
                    if (json.optBoolean("success", false)) {
                        val url = json.getString("url")
                        Log.d("YouTubeAudioExtractor", "Backend returned audio URL successfully")
                        return url
                    } else {
                        val error = json.optString("error", "Unknown error")
                        Log.w("YouTubeAudioExtractor", "Backend returned error: $error")
                    }
                }
            } else {
                Log.w("YouTubeAudioExtractor", "Backend service returned error code: ${response.code}")
                val errorBody = response.body?.string()
                Log.w("YouTubeAudioExtractor", "Backend error response: $errorBody")
                
                if (response.code == 404) {
                    Log.d("YouTubeAudioExtractor", "Video not found or no audio stream available")
                } else if (response.code == 503) {
                    Log.e("YouTubeAudioExtractor", "Backend server unavailable (503). Check if backend is running and accessible at: $backendUrl")
                } else if (response.code >= 500) {
                    Log.e("YouTubeAudioExtractor", "Backend server error (${response.code}). Server may be overloaded or misconfigured.")
                }
            }
            
            null
            
        } catch (e: Exception) {
            Log.w("YouTubeAudioExtractor", "Backend service error: ${e.message}")
            null
        }
    }
    
    /**
     * Tries direct YouTube extraction (fallback method).
     * This may not work reliably due to YouTube's restrictions.
     */
    private suspend fun tryDirectYouTubeExtraction(videoId: String): String? {
        return try {
            // Method: Use YouTube's get_video_info endpoint
            val videoInfoUrl = "https://www.youtube.com/get_video_info?video_id=$videoId&el=detailpage"
            
            val request = Request.Builder()
                .url(videoInfoUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val streamUrl = parseStreamUrlFromResponse(responseBody, videoId)
                    if (streamUrl != null) {
                        return streamUrl
                    }
                }
            }
            
            null
        } catch (e: Exception) {
            Log.e("YouTubeAudioExtractor", "Error in direct YouTube extraction", e)
            null
        }
    }
    
    /**
     * Parses stream URL from YouTube's get_video_info response.
     */
    private fun parseStreamUrlFromResponse(responseBody: String, videoId: String): String? {
        return try {
            // Parse the URL-encoded response
            val params = responseBody.split("&")
            var playerResponse: String? = null
            
            for (param in params) {
                if (param.startsWith("player_response=")) {
                    playerResponse = URLDecoder.decode(param.substring("player_response=".length), "UTF-8")
                    break
                }
            }
            
            if (playerResponse != null) {
                val json = JSONObject(playerResponse)
                val streamingData = json.optJSONObject("streamingData")
                
                if (streamingData != null) {
                    // Try to get adaptive formats (which include audio)
                    val formats = streamingData.optJSONArray("adaptiveFormats")
                    if (formats != null) {
                        // Find audio-only format
                        for (i in 0 until formats.length()) {
                            val format = formats.getJSONObject(i)
                            val mimeType = format.optString("mimeType", "")
                            if (mimeType.contains("audio") && format.has("url")) {
                                val url = format.getString("url")
                                Log.d("YouTubeAudioExtractor", "Found audio format: $mimeType")
                                return url
                            }
                        }
                    }
                }
            }
            
            null
        } catch (e: Exception) {
            Log.e("YouTubeAudioExtractor", "Error parsing stream URL", e)
            null
        }
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
     * 
     * For USB cable testing with ADB port forwarding:
     * 1. Connect phone via USB
     * 2. Run: adb reverse tcp:5000 tcp:5000
     * 3. Run: adb reverse tcp:3000 tcp:3000
     * 4. Change return to: "http://127.0.0.1:$port"
     */
    private fun getBackendUrl(port: Int): String {
        // For emulator, use 10.0.2.2 which maps to localhost
        if (isRunningOnEmulator()) {
            return "http://10.0.2.2:$port"
        }
        
        // For physical device:
        // Option 1: USB with ADB port forwarding - use localhost (uncomment if using port forwarding)
        // return "http://127.0.0.1:$port"  // Requires: adb reverse tcp:$port tcp:$port
        
        // Option 2: WiFi connection - use computer's IP
        return "http://10.77.146.73:$port"  // Your computer's IP for WiFi connection
    }
}




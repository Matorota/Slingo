package lt.viko.eif.mtrimaitis.Slingo.viewmodel

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import lt.viko.eif.mtrimaitis.Slingo.data.SongRepository
import lt.viko.eif.mtrimaitis.Slingo.data.models.Song
import java.io.IOException

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Int = 0,
    val duration: Int = 0,
    val playlist: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val errorMessage: String? = null
)

class MusicPlayerViewModel(
    private val songRepository: SongRepository,
    private val context: Context? = null
) : ViewModel() {
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var positionUpdateJob: Job? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    init {
        context?.let {
            audioManager = it.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }
    }

    fun loadSong(song: Song, autoPlay: Boolean = false) {
        viewModelScope.launch {
            stopPlayback()

            _playerState.value = _playerState.value.copy(
                currentSong = song,
                currentPosition = 0,
                duration = 0,
                isPlaying = false,
                errorMessage = null
            )

            if (song.previewUrl.isBlank()) {
                _playerState.value = _playerState.value.copy(
                    duration = if (song.duration > 0) song.duration else 0,
                    currentPosition = 0,
                    isPlaying = false,
                    errorMessage = null
                )
                android.util.Log.w("MusicPlayer", "Song has no preview URL: ${song.name}")
                return@launch
            }

            try {
                val audioFocusGranted = requestAudioFocus()
                if (!audioFocusGranted) {
                    android.util.Log.w("MusicPlayer", "Audio focus not granted")
                }

                mediaPlayer = MediaPlayer().apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        setAudioStreamType(AudioManager.STREAM_MUSIC)
                    }

                    try {
                        val mediaUri = Uri.parse(song.previewUrl)
                        val playbackContext = this@MusicPlayerViewModel.context
                        if (playbackContext != null) {
                            setDataSource(playbackContext, mediaUri)
                        } else {
                            setDataSource(song.previewUrl)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MusicPlayer", "Invalid media URI: ${song.previewUrl}", e)
                        _playerState.value = _playerState.value.copy(
                            errorMessage = "Song preview is unavailable."
                        )
                        return@launch
                    }
                    setOnPreparedListener { mp ->
                        android.util.Log.d("MusicPlayer", "MediaPlayer prepared, duration: ${mp.duration}ms")
                        val durationSeconds = if (mp.duration > 0) mp.duration / 1000 else 0
                        viewModelScope.launch {
                            _playerState.value = _playerState.value.copy(
                                duration = durationSeconds,
                                currentPosition = 0
                            )

                            if (autoPlay) {
                                try {
                                    if (mp.duration > 0) {
                                        mp.start()
                                        android.util.Log.d("MusicPlayer", "MediaPlayer started automatically")
                                        startPositionUpdates()
                                        _playerState.value = _playerState.value.copy(isPlaying = true)
                                    } else {
                                        android.util.Log.w("MusicPlayer", "Invalid duration, cannot play")
                                    }
                                } catch (e: IllegalStateException) {
                                    android.util.Log.e("MusicPlayer", "IllegalStateException starting playback: ${e.message}", e)
                                    mp.reset()
                                    try {
                                        mp.setDataSource(song.previewUrl)
                                        mp.prepareAsync()
                                    } catch (ex: Exception) {
                                        android.util.Log.e("MusicPlayer", "Error retrying: ${ex.message}", ex)
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("MusicPlayer", "Error starting playback: ${e.message}", e)
                                }
                            }
                        }
                    }
                    setOnErrorListener { mp, what, extra ->
                        android.util.Log.e("MusicPlayer", "MediaPlayer error: what=$what, extra=$extra")
                        viewModelScope.launch {
                            _playerState.value = _playerState.value.copy(isPlaying = false)
                        }
                        mp.reset()
                        false
                    }
                    setOnCompletionListener {
                        android.util.Log.d("MusicPlayer", "Playback completed")
                        viewModelScope.launch {
                            _playerState.value = _playerState.value.copy(
                                isPlaying = false,
                                currentPosition = _playerState.value.duration
                            )
                            positionUpdateJob?.cancel()
                            releaseAudioFocus()
                        }
                        playNext()
                    }
                    prepareAsync()
                    android.util.Log.d("MusicPlayer", "Preparing MediaPlayer with URL: ${song.previewUrl}")
                }
            } catch (e: IOException) {
                android.util.Log.e("MusicPlayer", "IOException loading song: ${e.message}", e)
                _playerState.value = _playerState.value.copy(
                    errorMessage = "Couldn't start playback. Check your connection and try again."
                )
            } catch (e: Exception) {
                android.util.Log.e("MusicPlayer", "Error loading song: ${e.message}", e)
                _playerState.value = _playerState.value.copy(
                    errorMessage = "Playback failed: ${e.message}"
                )
            }
        }
    }

    fun playPause() {
        val currentSong = _playerState.value.currentSong
        if (currentSong == null) {
            android.util.Log.w("MusicPlayer", "No current song to play")
            return
        }

        mediaPlayer?.let { player ->
            try {
                if (_playerState.value.isPlaying && player.isPlaying) {
                    player.pause()
                    android.util.Log.d("MusicPlayer", "Playback paused")
                    _playerState.value = _playerState.value.copy(isPlaying = false)
                } else {
                    val audioFocusGranted = requestAudioFocus()
                    if (!audioFocusGranted) {
                        android.util.Log.w("MusicPlayer", "Audio focus not granted, cannot play")
                        return
                    }

                    try {
                        if (player.duration > 0) {
                            player.start()
                            android.util.Log.d("MusicPlayer", "Playback started")
                            startPositionUpdates()
                            _playerState.value = _playerState.value.copy(isPlaying = true)
                        } else {
                            android.util.Log.w("MusicPlayer", "Player not prepared yet, waiting...")
                            viewModelScope.launch {
                                delay(500)
                                if (player.duration > 0) {
                                    player.start()
                                    startPositionUpdates()
                                    _playerState.value = _playerState.value.copy(isPlaying = true)
                                } else {
                                    android.util.Log.e("MusicPlayer", "Player still not prepared, reloading")
                                    loadSong(currentSong, autoPlay = true)
                                }
                            }
                        }
                    } catch (e: IllegalStateException) {
                        android.util.Log.e("MusicPlayer", "IllegalStateException: ${e.message}, reloading")
                        loadSong(currentSong, autoPlay = true)
                    } catch (e: Exception) {
                        android.util.Log.e("MusicPlayer", "Error starting playback: ${e.message}", e)
                        loadSong(currentSong, autoPlay = true)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MusicPlayer", "Error in playPause: ${e.message}", e)
                if (currentSong.previewUrl.isNotEmpty()) {
                    loadSong(currentSong, autoPlay = true)
                }
            }
        } ?: run {
            android.util.Log.d("MusicPlayer", "No media player, loading song")
            if (currentSong.previewUrl.isNotEmpty()) {
                loadSong(currentSong, autoPlay = true)
            } else {
                android.util.Log.w("MusicPlayer", "No preview URL available")
            }
        }
    }

    fun playNext() {
        val playlist = _playerState.value.playlist
        val currentIndex = _playerState.value.currentIndex

        if (playlist.isNotEmpty() && currentIndex < playlist.size - 1) {
            val nextIndex = currentIndex + 1
            val nextSong = playlist[nextIndex]
            _playerState.value = _playerState.value.copy(currentIndex = nextIndex)
            loadSong(nextSong, autoPlay = true)
        }
    }

    fun playPrevious() {
        val playlist = _playerState.value.playlist
        val currentIndex = _playerState.value.currentIndex

        if (playlist.isNotEmpty() && currentIndex > 0) {
            val prevIndex = currentIndex - 1
            val prevSong = playlist[prevIndex]
            _playerState.value = _playerState.value.copy(currentIndex = prevIndex)
            loadSong(prevSong, autoPlay = true)
        }
    }

    fun setPlaylist(playlist: List<Song>, startIndex: Int = 0, autoPlay: Boolean = false) {
        _playerState.value = _playerState.value.copy(
            playlist = playlist,
            currentIndex = startIndex,
            errorMessage = null
        )
        if (playlist.isNotEmpty() && startIndex < playlist.size) {
            loadSong(playlist[startIndex], autoPlay = autoPlay)
        }
    }

    fun seekTo(position: Int) {
        mediaPlayer?.let { player ->
            try {
                val positionMs = position * 1000
                if (player.duration > 0 && positionMs >= 0 && positionMs <= player.duration) {
                    player.seekTo(positionMs)
                    _playerState.value = _playerState.value.copy(currentPosition = position)
                    android.util.Log.d("MusicPlayer", "Seeked to position: ${position}s")
                }
            } catch (e: Exception) {
                android.util.Log.e("MusicPlayer", "Error seeking: ${e.message}", e)
            }
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            var shouldContinue = true
            while (isActive && shouldContinue) {
                val player = mediaPlayer ?: break

                try {
                    val durationMs = runCatching { player.duration }.getOrDefault(-1)
                    val currentPosMs = runCatching { player.currentPosition }.getOrDefault(-1)
                    val isCurrentlyPlaying = runCatching { player.isPlaying }.getOrDefault(false)

                    if (durationMs <= 0) {
                        if (durationMs == -1) {
                            android.util.Log.d("MusicPlayer", "MediaPlayer released, stopping updates")
                            shouldContinue = false
                        }
                    } else {
                        val currentPosSeconds = if (currentPosMs >= 0) currentPosMs / 1000 else 0
                        val durationSeconds = durationMs / 1000

                        _playerState.value = _playerState.value.copy(
                            currentPosition = currentPosSeconds,
                            duration = durationSeconds,
                            isPlaying = isCurrentlyPlaying
                        )
                    }
                } catch (e: IllegalStateException) {
                    android.util.Log.d("MusicPlayer", "Player state exception, stopping updates", e)
                    shouldContinue = false
                } catch (e: Exception) {
                    android.util.Log.e("MusicPlayer", "Error updating position: ${e.message}", e)
                    shouldContinue = false
                }

                if (shouldContinue) {
                    delay(200)
                }
            }
            android.util.Log.d("MusicPlayer", "Position updates stopped")
        }
    }

    private fun requestAudioFocus(): Boolean {
        audioManager?.let { am ->
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    build()
                }
                am.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        }
        return true
    }

    private fun releaseAudioFocus() {
        audioManager?.let { am ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(null)
            }
        }
    }

    private fun stopPlayback() {
        positionUpdateJob?.cancel()
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
        } catch (e: Exception) {
            android.util.Log.e("MusicPlayer", "Error stopping playback: ${e.message}")
        }
        mediaPlayer = null
        releaseAudioFocus()
        _playerState.value = _playerState.value.copy(isPlaying = false)
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
}


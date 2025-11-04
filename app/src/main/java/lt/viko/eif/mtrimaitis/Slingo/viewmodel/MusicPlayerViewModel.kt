package lt.viko.eif.mtrimaitis.Slingo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lt.viko.eif.mtrimaitis.Slingo.data.SongRepository
import lt.viko.eif.mtrimaitis.Slingo.data.models.Song
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import java.io.IOException

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Int = 0,
    val duration: Int = 0,
    val playlist: List<Song> = emptyList(),
    val currentIndex: Int = -1
)

class MusicPlayerViewModel(
    private val songRepository: SongRepository,
    private val context: Context? = null
) : ViewModel() {
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var positionUpdateJob: kotlinx.coroutines.Job? = null
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
                duration = song.duration
            )

            if (song.previewUrl.isNotEmpty()) {
                try {
                    // Request audio focus
                    val audioFocusGranted = requestAudioFocus()
                    if (!audioFocusGranted) {
                        android.util.Log.w("MusicPlayer", "Audio focus not granted")
                    }
                    
                    mediaPlayer = MediaPlayer().apply {
                        // Set audio attributes for better compatibility
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
                        
                        setDataSource(song.previewUrl)
                        setOnPreparedListener { mp ->
                            android.util.Log.d("MusicPlayer", "MediaPlayer prepared, duration: ${mp.duration}ms")
                            viewModelScope.launch {
                                _playerState.value = _playerState.value.copy(
                                    duration = mp.duration / 1000
                                )
                                if (autoPlay) {
                                    try {
                                        mp.start()
                                        android.util.Log.d("MusicPlayer", "MediaPlayer started")
                                        startPositionUpdates()
                                        _playerState.value = _playerState.value.copy(isPlaying = true)
                                    } catch (e: Exception) {
                                        android.util.Log.e("MusicPlayer", "Error starting playback: ${e.message}", e)
                                    }
                                }
                            }
                        }
                        setOnErrorListener { mp, what, extra ->
                            android.util.Log.e("MusicPlayer", "MediaPlayer error: what=$what, extra=$extra")
                            mp.reset()
                            false
                        }
                        setOnCompletionListener {
                            android.util.Log.d("MusicPlayer", "Playback completed")
                            stopPlayback()
                            releaseAudioFocus()
                            playNext()
                        }
                        prepareAsync()
                        android.util.Log.d("MusicPlayer", "Preparing MediaPlayer with URL: ${song.previewUrl}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MusicPlayer", "Error loading song: ${e.message}", e)
                    e.printStackTrace()
                }
            } else {
                android.util.Log.w("MusicPlayer", "Song has no preview URL: ${song.name}")
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
                if (_playerState.value.isPlaying) {
                    // Pause
                    if (player.isPlaying) {
                        player.pause()
                        android.util.Log.d("MusicPlayer", "Playback paused")
                    }
                    positionUpdateJob?.cancel()
                    _playerState.value = _playerState.value.copy(isPlaying = false)
                } else {
                    // Play
                    val audioFocusGranted = requestAudioFocus()
                    if (!audioFocusGranted) {
                        android.util.Log.w("MusicPlayer", "Audio focus not granted, cannot play")
                        return
                    }
                    
                    // Check if player is prepared
                    if (player.isPlaying) {
                        android.util.Log.d("MusicPlayer", "Player already playing")
                    } else {
                        try {
                            player.start()
                            android.util.Log.d("MusicPlayer", "Playback started")
                            startPositionUpdates()
                            _playerState.value = _playerState.value.copy(isPlaying = true)
                        } catch (e: IllegalStateException) {
                            android.util.Log.e("MusicPlayer", "Player not prepared, reloading: ${e.message}")
                            // Player not prepared, reload
                            loadSong(currentSong, autoPlay = true)
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MusicPlayer", "Error in playPause: ${e.message}", e)
                // If player is not ready, try to reload
                if (currentSong.previewUrl.isNotEmpty()) {
                    loadSong(currentSong, autoPlay = true)
                }
            }
        } ?: run {
            // If no media player, load and play
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
            loadSong(nextSong)
            playPause()
        }
    }

    fun playPrevious() {
        val playlist = _playerState.value.playlist
        val currentIndex = _playerState.value.currentIndex
        
        if (playlist.isNotEmpty() && currentIndex > 0) {
            val prevIndex = currentIndex - 1
            val prevSong = playlist[prevIndex]
            _playerState.value = _playerState.value.copy(currentIndex = prevIndex)
            loadSong(prevSong)
            playPause()
        }
    }

    fun setPlaylist(playlist: List<Song>, startIndex: Int = 0, autoPlay: Boolean = false) {
        _playerState.value = _playerState.value.copy(
            playlist = playlist,
            currentIndex = startIndex
        )
        if (playlist.isNotEmpty() && startIndex < playlist.size) {
            loadSong(playlist[startIndex], autoPlay = autoPlay)
        }
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position * 1000)
        _playerState.value = _playerState.value.copy(currentPosition = position)
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (_playerState.value.isPlaying && mediaPlayer != null) {
                try {
                    mediaPlayer?.let { player ->
                        if (player.isPlaying) {
                            val currentPos = player.currentPosition / 1000
                            _playerState.value = _playerState.value.copy(currentPosition = currentPos)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MusicPlayer", "Error updating position: ${e.message}")
                    break
                }
                kotlinx.coroutines.delay(500) // Update every 500ms for smoother progress
            }
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
        return true // If no audio manager, assume granted
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


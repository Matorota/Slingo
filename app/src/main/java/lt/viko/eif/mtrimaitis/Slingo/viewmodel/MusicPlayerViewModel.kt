package lt.viko.eif.mtrimaitis.Slingo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lt.viko.eif.mtrimaitis.Slingo.data.SongRepository
import lt.viko.eif.mtrimaitis.Slingo.data.models.Song
import android.media.MediaPlayer
import java.io.IOException

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Int = 0,
    val duration: Int = 0,
    val playlist: List<Song> = emptyList(),
    val currentIndex: Int = -1
)

class MusicPlayerViewModel(private val songRepository: SongRepository) : ViewModel() {
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var positionUpdateJob: kotlinx.coroutines.Job? = null

    fun loadSong(song: Song) {
        viewModelScope.launch {
            stopPlayback()
            
            _playerState.value = _playerState.value.copy(
                currentSong = song,
                currentPosition = 0,
                duration = song.duration
            )

            if (song.previewUrl.isNotEmpty()) {
                try {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(song.previewUrl)
                        prepareAsync()
                        setOnPreparedListener {
                            _playerState.value = _playerState.value.copy(
                                duration = it.duration / 1000
                            )
                        }
                        setOnCompletionListener {
                            stopPlayback()
                            playNext()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun playPause() {
        val currentSong = _playerState.value.currentSong
        if (currentSong == null) return

        mediaPlayer?.let { player ->
            if (_playerState.value.isPlaying) {
                player.pause()
                positionUpdateJob?.cancel()
                _playerState.value = _playerState.value.copy(isPlaying = false)
            } else {
                player.start()
                startPositionUpdates()
                _playerState.value = _playerState.value.copy(isPlaying = true)
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

    fun setPlaylist(playlist: List<Song>, startIndex: Int = 0) {
        _playerState.value = _playerState.value.copy(
            playlist = playlist,
            currentIndex = startIndex
        )
        if (playlist.isNotEmpty() && startIndex < playlist.size) {
            loadSong(playlist[startIndex])
        }
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position * 1000)
        _playerState.value = _playerState.value.copy(currentPosition = position)
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (_playerState.value.isPlaying) {
                mediaPlayer?.let { player ->
                    val currentPos = player.currentPosition / 1000
                    _playerState.value = _playerState.value.copy(currentPosition = currentPos)
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun stopPlayback() {
        positionUpdateJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        _playerState.value = _playerState.value.copy(isPlaying = false)
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
}


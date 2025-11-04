package lt.viko.eif.mtrimaitis.Slingo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lt.viko.eif.mtrimaitis.Slingo.data.PlaylistRepository
import lt.viko.eif.mtrimaitis.Slingo.data.models.Playlist
import lt.viko.eif.mtrimaitis.Slingo.data.models.Song

data class PlaylistUiState(
    val playlists: List<Playlist> = emptyList(),
    val currentPlaylistSongs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PlaylistViewModel(
    private val playlistRepository: PlaylistRepository,
    private val userId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            playlistRepository.getPlaylistsByUser(userId).collect { playlists ->
                _uiState.value = _uiState.value.copy(playlists = playlists)
            }
        }
    }

    fun createPlaylist(name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            playlistRepository.createPlaylist(name, userId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            playlistRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun loadPlaylistSongs(playlistId: Long) {
        viewModelScope.launch {
            playlistRepository.getSongsInPlaylist(playlistId).collect { songs ->
                _uiState.value = _uiState.value.copy(currentPlaylistSongs = songs)
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlist)
        }
    }
}


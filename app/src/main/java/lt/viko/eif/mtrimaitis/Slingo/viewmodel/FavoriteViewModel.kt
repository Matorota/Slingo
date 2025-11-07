package lt.viko.eif.mtrimaitis.Slingo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lt.viko.eif.mtrimaitis.Slingo.data.FavoriteRepository
import lt.viko.eif.mtrimaitis.Slingo.data.models.Song

data class FavoriteUiState(
    val favoriteSongs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class FavoriteViewModel(private val favoriteRepository: FavoriteRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            favoriteRepository.getFavoriteSongs().collect { songs ->
                _uiState.value = _uiState.value.copy(favoriteSongs = songs)
            }
        }
    }

    suspend fun isFavorite(songId: String): Boolean {
        return favoriteRepository.isFavorite(songId)
    }

    fun toggleFavorite(songId: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val isFavorite = favoriteRepository.toggleFavorite(songId)
            onResult(isFavorite)
        }
    }

    fun addFavorite(songId: String) {
        viewModelScope.launch {
            favoriteRepository.addFavorite(songId)
        }
    }

    fun removeFavorite(songId: String) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(songId)
        }
    }
}


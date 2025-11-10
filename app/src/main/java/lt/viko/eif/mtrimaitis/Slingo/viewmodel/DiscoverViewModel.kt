package lt.viko.eif.mtrimaitis.Slingo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lt.viko.eif.mtrimaitis.Slingo.data.SongRepository
import lt.viko.eif.mtrimaitis.Slingo.data.models.Song

data class DiscoverUiState(
    val searchResults: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val recommendations: List<Song> = emptyList(),
    val isLoadingRecommendations: Boolean = false
)

class DiscoverViewModel(private val songRepository: SongRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    init {
        loadRecommendations()
    }

    fun searchTracks(query: String) {
        val trimmed = query.trim()
        searchJob?.cancel()

        if (trimmed.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                searchQuery = "",
                isLoading = false
            )
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                searchQuery = trimmed
            )

            songRepository.searchTracks(trimmed)
                .onSuccess { tracks ->
                    _uiState.value = _uiState.value.copy(
                        searchResults = tracks,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to search tracks"
                    )
                }
        }
    }

    fun searchTracksImmediate(query: String) {
        val trimmed = query.trim()
        searchJob?.cancel()

        if (trimmed.isBlank()) {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                searchQuery = "",
                isLoading = false
            )
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                searchQuery = trimmed
            )

            songRepository.searchTracks(trimmed)
                .onSuccess { tracks ->
                    _uiState.value = _uiState.value.copy(
                        searchResults = tracks,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to search tracks"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRecommendations = true)
            songRepository.getRecommendedSongs()
                .onSuccess { songs ->
                    _uiState.value = _uiState.value.copy(
                        recommendations = songs,
                        isLoadingRecommendations = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingRecommendations = false,
                        errorMessage = exception.message ?: "Failed to load recommendations"
                    )
                }
        }
    }
}


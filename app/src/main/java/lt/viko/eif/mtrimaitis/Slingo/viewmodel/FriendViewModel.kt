package lt.viko.eif.mtrimaitis.Slingo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import lt.viko.eif.mtrimaitis.Slingo.data.FriendRepository
import lt.viko.eif.mtrimaitis.Slingo.data.SharedPlaylistRepository
import lt.viko.eif.mtrimaitis.Slingo.data.dao.UserDao
import lt.viko.eif.mtrimaitis.Slingo.data.models.FriendRequest
import lt.viko.eif.mtrimaitis.Slingo.data.models.Friendship
import lt.viko.eif.mtrimaitis.Slingo.data.models.SharedPlaylist
import lt.viko.eif.mtrimaitis.Slingo.data.models.User

data class FriendUiState(
    val searchQuery: String = "",
    val searchResults: List<User> = emptyList(),
    val pendingRequests: List<FriendRequest> = emptyList(),
    val sentRequests: List<FriendRequest> = emptyList(),
    val friendships: List<Friendship> = emptyList(),
    val friends: List<User> = emptyList(),
    val pendingSharedPlaylists: List<SharedPlaylist> = emptyList(),
    val acceptedSharedPlaylists: List<SharedPlaylist> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class FriendViewModel(
    private val friendRepository: FriendRepository,
    private val sharedPlaylistRepository: SharedPlaylistRepository,
    private val userDao: UserDao,
    private val currentUserId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(FriendUiState())
    val uiState: StateFlow<FriendUiState> = _uiState.asStateFlow()

    init {
        loadPendingRequests()
        loadSentRequests()
        loadFriendships()
        loadPendingSharedPlaylists()
        loadAcceptedSharedPlaylists()
    }

    fun searchUsers(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val results = friendRepository.searchUsers(query, currentUserId)
                _uiState.value = _uiState.value.copy(
                    searchResults = results,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun sendFriendRequest(toUserId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            friendRepository.sendFriendRequest(currentUserId, toUserId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadSentRequests()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
        }
    }

    fun acceptFriendRequest(requestId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            friendRepository.acceptFriendRequest(requestId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadPendingRequests()
                    loadFriendships()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
        }
    }

    fun declineFriendRequest(requestId: Long) {
        viewModelScope.launch {
            friendRepository.declineFriendRequest(requestId)
            loadPendingRequests()
        }
    }

    fun sharePlaylist(playlistId: Long, toUserId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            sharedPlaylistRepository.sharePlaylist(playlistId, currentUserId, toUserId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
        }
    }

    fun acceptSharedPlaylist(sharedPlaylistId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            sharedPlaylistRepository.acceptSharedPlaylist(sharedPlaylistId, currentUserId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadPendingSharedPlaylists()
                    loadAcceptedSharedPlaylists()
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

    fun declineSharedPlaylist(sharedPlaylistId: Long) {
        viewModelScope.launch {
            sharedPlaylistRepository.declineSharedPlaylist(sharedPlaylistId)
            loadPendingSharedPlaylists()
        }
    }

    private fun loadPendingRequests() {
        viewModelScope.launch {
            friendRepository.getPendingRequestsForUser(currentUserId).collect { requests ->
                _uiState.value = _uiState.value.copy(pendingRequests = requests)
            }
        }
    }

    private fun loadSentRequests() {
        viewModelScope.launch {
            friendRepository.getSentRequestsByUser(currentUserId).collect { requests ->
                _uiState.value = _uiState.value.copy(sentRequests = requests)
            }
        }
    }

    private fun loadFriendships() {
        viewModelScope.launch {
            friendRepository.getFriendshipsForUser(currentUserId).collect { friendships ->
                _uiState.value = _uiState.value.copy(friendships = friendships)
                // Load friend user details
                val friendIds = friendships.map { friendship ->
                    if (friendship.userId1 == currentUserId) friendship.userId2 else friendship.userId1
                }
                val friends = friendIds.mapNotNull { friendId ->
                    userDao.getUserById(friendId).first()
                }
                _uiState.value = _uiState.value.copy(friends = friends)
            }
        }
    }

    private fun loadPendingSharedPlaylists() {
        viewModelScope.launch {
            sharedPlaylistRepository.getPendingSharedPlaylistsForUser(currentUserId).collect { sharedPlaylists ->
                _uiState.value = _uiState.value.copy(pendingSharedPlaylists = sharedPlaylists)
            }
        }
    }

    private fun loadAcceptedSharedPlaylists() {
        viewModelScope.launch {
            sharedPlaylistRepository.getAcceptedSharedPlaylistsForUser(currentUserId).collect { sharedPlaylists ->
                _uiState.value = _uiState.value.copy(acceptedSharedPlaylists = sharedPlaylists)
            }
        }
    }

    suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId).first()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}


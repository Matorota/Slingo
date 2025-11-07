package lt.viko.eif.mtrimaitis.Slingo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lt.viko.eif.mtrimaitis.Slingo.data.LoginRepository
import lt.viko.eif.mtrimaitis.Slingo.data.models.User

data class AuthUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(private val loginRepository: LoginRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(username: String, email: String, password: String, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            loginRepository.registerUser(username, email, password)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        currentUser = user,
                        isLoading = false,
                        errorMessage = null
                    )
                    onSuccess(user)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
        }
    }

    fun login(email: String, password: String, onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            loginRepository.loginUser(email, password)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        currentUser = user,
                        isLoading = false,
                        errorMessage = null
                    )
                    onSuccess(user)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
        }
    }

    fun logout() {
        _uiState.value = AuthUiState()
    }

    fun verifyCurrentPassword(password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val currentUser = _uiState.value.currentUser
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(errorMessage = "Not logged in")
                onResult(false)
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            loginRepository.verifyPassword(currentUser.id, password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                    onResult(true)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                    onResult(false)
                }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentUser = _uiState.value.currentUser
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(errorMessage = "Not logged in")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            loginRepository.changePassword(currentUser.id, oldPassword, newPassword)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}


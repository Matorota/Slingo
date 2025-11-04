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

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}


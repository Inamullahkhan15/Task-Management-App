package com.exmaple.taskmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exmaple.taskmanagement.repository.AuthRepository
import com.exmaple.taskmanagement.repository.EmailNotVerifiedException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val role: String, val uid: String) : LoginUiState
    data class Error(val message: String) : LoginUiState
    data class UnverifiedEmail(val email: String, val password: String) : LoginUiState
}

sealed interface SignUpUiState {
    object Idle : SignUpUiState
    object Loading : SignUpUiState
    object Success : SignUpUiState
    data class Error(val message: String) : SignUpUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _requestState = MutableStateFlow<ActionState>(ActionState.Idle)
    val requestState: StateFlow<ActionState> = _requestState.asStateFlow()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _signUpState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val signUpState: StateFlow<SignUpUiState> = _signUpState.asStateFlow()

    // Remembers the logged-in user's role for the whole app session —
    // used by NavGraph to guard admin-only routes.
    private val _currentRole = MutableStateFlow<String?>(null)
    val currentRole: StateFlow<String?> = _currentRole.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<com.exmaple.taskmanagement.model.User?>(null)
    val currentUserProfile: StateFlow<com.exmaple.taskmanagement.model.User?> = _currentUserProfile.asStateFlow()

    fun handleJoinOrRegister(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Please fill in all fields")
            return
        }
        if (password.length < 6) {
            _uiState.value = LoginUiState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _requestState.value = ActionState.Loading
            
            // Join Request effectively creates a pending account
            val result = authRepository.signUp(name.trim(), email.trim(), password)
            result.fold(
                onSuccess = { 
                    _requestState.value = ActionState.Success 
                },
                onFailure = { e -> 
                    _requestState.value = ActionState.Error(e.localizedMessage ?: "Request failed")
                }
            )
        }
    }

    // Deleted custom await to use the official extension from kotlinx-coroutines-play-services

    fun signUp(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _signUpState.value = SignUpUiState.Error("Please fill in all fields")
            return
        }
        if (password.length < 6) {
            _signUpState.value = SignUpUiState.Error("Password must be at least 6 characters")
            return
        }
        if (password != confirmPassword) {
            _signUpState.value = SignUpUiState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            _signUpState.value = SignUpUiState.Loading
            val result = authRepository.signUp(name.trim(), email.trim(), password)
            result.fold(
                onSuccess = { _signUpState.value = SignUpUiState.Success },
                onFailure = { e -> _signUpState.value = SignUpUiState.Error(e.localizedMessage ?: "Sign up failed") }
            )
        }
    }

    fun resetSignUpState() {
        _signUpState.value = SignUpUiState.Idle
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val loginResult = authRepository.login(email.trim(), password)
            loginResult.fold(
                onSuccess = { uid ->
                    val role = authRepository.getUserRole(uid)
                    if (role != null) {
                        _currentRole.value = role
                        loadUserProfile(uid)
                        _uiState.value = LoginUiState.Success(role = role, uid = uid)
                    } else {
                        _uiState.value = LoginUiState.Error("User role not found in database")
                    }
                },
                onFailure = { error ->
                    _uiState.value = if (error is EmailNotVerifiedException) {
                        LoginUiState.UnverifiedEmail(email.trim(), password)
                    } else {
                        LoginUiState.Error(error.localizedMessage ?: "Authentication failed")
                    }
                }
            )
        }
    }

    fun resendVerification(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.resendVerificationEmail(email, password)
            result.fold(
                onSuccess = { _uiState.value = LoginUiState.Error("Verification email sent again. Please check your inbox.") },
                onFailure = { e -> _uiState.value = LoginUiState.Error(e.localizedMessage ?: "Could not resend email") }
            )
        }
    }

    fun checkInitialSession(onRoleResolved: (role: String?, uid: String?) -> Unit) {
        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId()
            if (uid != null) {
                val role = authRepository.getUserRole(uid)
                _currentRole.value = role
                loadUserProfile(uid)
                onRoleResolved(role, uid)
            } else {
                _currentRole.value = null
                onRoleResolved(null, null)
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = LoginUiState.Idle
        _currentRole.value = null
        _currentUserProfile.value = null
    }

    private fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            val user = authRepository.getUserProfile(uid)
            _currentUserProfile.value = user
        }
    }

    fun getCurrentUserId(): String? = authRepository.getCurrentUserId()

    fun resetRequestState() {
        _requestState.value = ActionState.Idle
    }

    private val _resetPasswordState = MutableStateFlow<ActionState>(ActionState.Idle)
    val resetPasswordState: StateFlow<ActionState> = _resetPasswordState.asStateFlow()

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _resetPasswordState.value = ActionState.Error("Please enter your email address")
            return
        }
        viewModelScope.launch {
            _resetPasswordState.value = ActionState.Loading
            val result = authRepository.sendPasswordResetEmail(email)
            result.fold(
                onSuccess = { _resetPasswordState.value = ActionState.Success },
                onFailure = { e -> _resetPasswordState.value = ActionState.Error(e.localizedMessage ?: "Failed to send reset email") }
            )
        }
    }

    fun resetPasswordState() {
        _resetPasswordState.value = ActionState.Idle
    }
}
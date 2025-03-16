package com.example.myapplication.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authService = MockAuthService()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState
    
    private val _currentUser = MutableStateFlow<MockAuthService.User?>(null)
    val currentUser: StateFlow<MockAuthService.User?> = _currentUser
    
    init {
        // Check if user is already signed in
        _currentUser.value = authService.getCurrentUser()
        _authState.value = if (authService.getCurrentUser() != null) {
            AuthState.Authenticated
        } else {
            AuthState.Unauthenticated
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            authService.signIn(email, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Authentication failed")
                }
        }
    }
    
    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            authService.register(email, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Registration failed")
                }
        }
    }
    
    fun signOut() {
        authService.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
} 
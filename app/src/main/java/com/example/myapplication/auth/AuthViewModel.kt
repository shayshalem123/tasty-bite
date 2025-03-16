package com.example.myapplication.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    // Firebase Auth for login
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    // Mock service for registration
    private val mockAuthService = MockAuthService()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState
    
    private val _currentUser = MutableStateFlow<UserData?>(null)
    val currentUser: StateFlow<UserData?> = _currentUser
    
    init {
        // Check if user is already signed in with Firebase
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            _currentUser.value = UserData(
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: ""
            )
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            try {
                // Use Firebase for login
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                
                if (user != null) {
                    _currentUser.value = UserData(
                        email = user.email ?: "",
                        displayName = user.displayName ?: user.email?.substringBefore("@") ?: ""
                    )
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error("Failed to get user information")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }
    
    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            // Use mock service for registration
            mockAuthService.register(email, password)
                .onSuccess { user ->
                    _currentUser.value = UserData(
                        email = user.email,
                        displayName = user.displayName
                    )
                    _authState.value = AuthState.Authenticated
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Registration failed")
                }
        }
    }
    
    fun signOut() {
        // Sign out from Firebase
        firebaseAuth.signOut()
        // Also sign out from mock service
        mockAuthService.signOut()
        
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }
    
    // Common user data class that can be used with both Firebase and mock auth
    data class UserData(val email: String, val displayName: String)
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
} 
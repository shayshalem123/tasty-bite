package com.example.myapplication.auth

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    // Firebase Auth for both login and registration
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState
    
    private val _currentUser = MutableStateFlow<UserData?>(null)
    val currentUser: StateFlow<UserData?> = _currentUser
    
    // Repository for profile image operations
    private val profileImageRepository = ProfileImageRepository()
    
    // State for profile image update operations
    private val _profileUpdateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Initial)
    val profileUpdateState: StateFlow<ProfileUpdateState> = _profileUpdateState
    
    init {
        // Check if user is already signed in with Firebase
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            _currentUser.value = UserData(
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "",
                profilePictureUrl = firebaseUser.photoUrl?.toString() ?: ""
            )
            Log.d("AuthViewModel", "Current user: ${_currentUser.value}")
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
                        displayName = user.displayName ?: user.email?.substringBefore("@") ?: "",
                        profilePictureUrl = user.photoUrl?.toString() ?: ""
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
    
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            try {
                // Use Firebase for registration
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                
                if (user != null) {
                    // Update the user's display name
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    
                    user.updateProfile(profileUpdates).await()
                    
                    _currentUser.value = UserData(
                        email = user.email ?: "",
                        displayName = name,
                        profilePictureUrl = user.photoUrl?.toString() ?: ""
                    )
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error("Failed to create user")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }
    
    fun updateProfilePicture(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _profileUpdateState.value = ProfileUpdateState.Loading
                
                val currentFirebaseUser = firebaseAuth.currentUser
                
                if (currentFirebaseUser != null) {
                    // Upload the image to Firebase Storage
                    val userId = currentFirebaseUser.uid
                    val downloadUrl = profileImageRepository.uploadProfileImage(imageUri, userId).getOrThrow()
                    
                    // Update the user's profile with the new photo URL
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(downloadUrl))
                        .build()
                    
                    currentFirebaseUser.updateProfile(profileUpdates).await()
                    
                    // Refresh user data
                    refreshUserData()
                    
                    _profileUpdateState.value = ProfileUpdateState.Success
                } else {
                    _profileUpdateState.value = ProfileUpdateState.Error("No user logged in")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating profile picture", e)
                _profileUpdateState.value = ProfileUpdateState.Error(e.message ?: "Failed to update profile picture")
            }
        }
    }
    
    fun resetProfileUpdateState() {
        _profileUpdateState.value = ProfileUpdateState.Initial
    }
    
    fun signOut() {
        // Sign out from Firebase
        firebaseAuth.signOut()
        
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }
    
    fun refreshUserData() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            _currentUser.value = UserData(
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "",
                profilePictureUrl = firebaseUser.photoUrl?.toString() ?: ""
            )
        }
    }
    
    // User data class for Firebase auth
    data class UserData(val email: String, val displayName: String, val profilePictureUrl: String = "")
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class ProfileUpdateState {
    object Initial : ProfileUpdateState()
    object Loading : ProfileUpdateState()
    object Success : ProfileUpdateState()
    data class Error(val message: String) : ProfileUpdateState()
} 
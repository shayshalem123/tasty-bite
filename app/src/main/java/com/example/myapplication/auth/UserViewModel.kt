package com.example.myapplication.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for user-related data operations
 * Uses UserService to fetch and cache user information
 */
class UserViewModel(private val userService: UserService) : ViewModel() {
    
    // Cache of user display names - Maps email to display name
    private val userDisplayNameMap = mutableMapOf<String, MutableStateFlow<String>>()
    
    /**
     * Get a user's display name as a StateFlow
     * Creates and caches a flow if one doesn't exist for the email
     * @param email The email to look up
     * @return A StateFlow of the user's display name
     */
    fun getUserDisplayName(email: String): StateFlow<String> {
        // Create a StateFlow for this email if it doesn't exist
        if (!userDisplayNameMap.containsKey(email)) {
            
            userDisplayNameMap[email] = MutableStateFlow(email.substringBefore('@'))
            
            // Start collecting the actual data
            fetchUserDisplayName(email)
        }
        
        return userDisplayNameMap[email]!!.asStateFlow()
    }
    
    /**
     * Fetch a user's display name from the service
     * @param email The email to look up
     */
    private fun fetchUserDisplayName(email: String) {
        viewModelScope.launch {
            userService.getUserDisplayName(email).collect { displayName ->
                userDisplayNameMap[email]?.value = displayName
            }
        }
    }
    
    /**
     * Refresh a user's display name
     * Forces a re-fetch from the service
     * @param email The email to refresh
     */
    fun refreshUserDisplayName(email: String) {
        userService.clearCacheForEmail(email)
        fetchUserDisplayName(email)
    }
    
    /**
     * Clear all cached user data
     */
    fun clearCache() {
        userService.clearCache()
        userDisplayNameMap.forEach { (email, _) ->
            fetchUserDisplayName(email)
        }
    }
    
    /**
     * Get the current user's email
     * @return The current user's email or a default value if not authenticated
     */
    fun getCurrentUserEmail(): String {
        return userService.getCurrentUser()?.email ?: "anonymous"
    }
    
    /**
     * Factory for creating UserViewModel with dependencies
     */
    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserViewModel(UserService()) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 
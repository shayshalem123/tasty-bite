package com.example.myapplication.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

/**
 * Service for managing user information from Firebase Authentication
 * Uses caching to avoid unnecessary network requests
 */
class UserService {
    private val TAG = "UserService"
    private val auth = FirebaseAuth.getInstance()
    
    // In-memory cache of user display names
    private val userDisplayNameCache = mutableMapOf<String, String>()
    
    /**
     * Gets the current authenticated user
     * @return The current user or null if not authenticated
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    /**
     * Gets the display name for a user based on their email
     * Uses cache to avoid unnecessary Firebase calls
     * @param email The email of the user
     * @return A Flow emitting the display name
     */
    fun getUserDisplayName(email: String): Flow<String> = flow {
        // Check cache first
        userDisplayNameCache[email]?.let {
            emit(it)
            return@flow
        }
        
        try {
            // If this is the current user, get their display name directly
            auth.currentUser?.let { currentUser ->
                if (currentUser.email == email) {
                    // Current user - use display name or fallback
                    val displayName = currentUser.displayName ?: email.substringBefore('@')
                    userDisplayNameCache[email] = displayName
                    emit(displayName)
                    return@flow
                }
            }
            
            // Otherwise try to get user info from Firebase
            val result = auth.fetchSignInMethodsForEmail(email).await()
            
            if (result.signInMethods?.isNotEmpty() == true) {
                // User exists in Firebase but not current user
                // We can't get other users' display names directly, so use our best guess
                // which is to use their email prefix as display name
                val defaultName = email.substringBefore('@')
                userDisplayNameCache[email] = defaultName
                emit(defaultName)
            } else {
                // Default to email username if user not found
                val defaultName = email.substringBefore('@')
                userDisplayNameCache[email] = defaultName
                emit(defaultName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user info for $email", e)
            // Default to email username if there's an error
            val defaultName = email.substringBefore('@')
            emit(defaultName)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get current user by email (if it matches current user)
     * @param email Email to check
     * @return FirebaseUser if current user matches email, null otherwise
     */
    private fun getCurrentUserByEmail(email: String): FirebaseUser? {
        return auth.currentUser?.takeIf { it.email == email }
    }
    
    /**
     * Clear the cache when needed (e.g., after profile updates)
     */
    fun clearCache() {
        userDisplayNameCache.clear()
    }
    
    /**
     * Clear a specific email from cache
     */
    fun clearCacheForEmail(email: String) {
        userDisplayNameCache.remove(email)
    }
} 
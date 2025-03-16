package com.example.myapplication.auth

import kotlinx.coroutines.delay

/**
 * A mock implementation of Firebase-like authentication service
 */
class MockAuthService {
    // Simple user storage - email -> password
    private val users = mutableMapOf(
        "test@example.com" to "password123"
    )
    
    private var currentUser: User? = null
    
    suspend fun signIn(email: String, password: String): Result<User> {
        // Simulate network delay
        delay(1000)
        
        return if (users.containsKey(email) && users[email] == password) {
            val user = User(email, email.substringBefore("@"))
            currentUser = user
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }
    
    suspend fun register(email: String, password: String): Result<User> {
        // Simulate network delay
        delay(1000)
        
        return if (users.containsKey(email)) {
            Result.failure(Exception("Email already in use"))
        } else {
            val user = User(email, email.substringBefore("@"))
            users[email] = password
            currentUser = user
            Result.success(user)
        }
    }
    
    fun signOut() {
        currentUser = null
    }
    
    fun getCurrentUser(): User? = currentUser
    
    data class User(val email: String, val displayName: String)
} 
package com.example.myapplication.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.myapplication.auth.AuthViewModel

/**
 * Authentication screen that shows either login or register screens
 * @param authViewModel The view model for authentication
 * @param error Optional error message to display
 */
@Composable
fun AuthenticationScreen(
    authViewModel: AuthViewModel,
    error: String? = null
) {
    // State to track whether to show login or register screen
    var showLoginScreen by remember { mutableStateOf(true) }
    
    if (showLoginScreen) {
        LoginScreen(
            authViewModel = authViewModel,
            onNavigateToRegister = { showLoginScreen = false },
            error = error
        )
    } else {
        RegisterScreen(
            authViewModel = authViewModel,
            onNavigateToLogin = { showLoginScreen = true },
            error = error
        )
    }
} 
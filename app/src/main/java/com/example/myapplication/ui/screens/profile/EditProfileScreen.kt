package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onProfileUpdated: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // State for form fields
    var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    isLoading = true
                                    errorMessage = null
                                    successMessage = null
                                    
                                    // Get current Firebase user
                                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                                    
                                    if (firebaseUser != null) {
                                        // Create profile update request
                                        val profileUpdates = UserProfileChangeRequest.Builder()
                                            .setDisplayName(displayName)
                                            .build()
                                        
                                        // Update profile
                                        firebaseUser.updateProfile(profileUpdates).await()
                                        
                                        // Update local state in AuthViewModel
                                        authViewModel.refreshUserData()
                                        
                                        successMessage = "Profile updated successfully"
                                        isLoading = false
                                        
                                        // Navigate back after a short delay
                                        kotlinx.coroutines.delay(1000)
                                        onProfileUpdated()
                                    } else {
                                        errorMessage = "User not found"
                                        isLoading = false
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Failed to update profile: ${e.message}"
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading && displayName.isNotBlank()
                    ) {

                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.firstOrNull()?.uppercase() ?: "U",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Display name field
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Email field (readonly)
            OutlinedTextField(
                value = currentUser?.email ?: "",
                onValueChange = { },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Show loading indicator
            if (isLoading) {
                CircularProgressIndicator()
            }
            
            // Show error message
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Show success message
            successMessage?.let { success ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = success,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
} 
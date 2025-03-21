package com.example.myapplication.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.auth.AuthViewModel
import com.example.myapplication.auth.ProfileUpdateState
import com.example.myapplication.ui.components.ProfileImage
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
    val profileUpdateState by authViewModel.profileUpdateState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // State for form fields
    var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Validate image
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)

            // Check file type
            val isValidType = mimeType == "image/jpeg" || mimeType == "image/png"

            // Check file size (limit to 2MB)
            val fileSize = try {
                contentResolver.openInputStream(uri)?.use { it.available() } ?: 0
            } catch (e: Exception) {
                -1
            }
            val isValidSize = fileSize in 1..2 * 1024 * 1024 // 2MB max

            when {
                !isValidType -> {
                    errorMessage = "Invalid format. Please select a JPEG or PNG image."
                }
                !isValidSize -> {
                    errorMessage = "Image is too large. Maximum size is 2MB."
                }
                else -> {
                    selectedImageUri = it
                    errorMessage = null
                }
            }
        }
    }
    
    // Update profile picture when selected
    LaunchedEffect(selectedImageUri) {
        if (selectedImageUri != null) {
            authViewModel.updateProfilePicture(selectedImageUri!!)
        }
    }
    
    // Monitor profile update state
    LaunchedEffect(profileUpdateState) {
        when (profileUpdateState) {
            is ProfileUpdateState.Loading -> {
                isLoading = true
                errorMessage = null
            }
            is ProfileUpdateState.Success -> {
                isLoading = false
                errorMessage = null
                successMessage = "Profile picture updated successfully"
                // Reset state
                authViewModel.resetProfileUpdateState()
            }
            is ProfileUpdateState.Error -> {
                isLoading = false
                errorMessage = (profileUpdateState as ProfileUpdateState.Error).message
                // Reset state
                authViewModel.resetProfileUpdateState()
            }
            else -> {
                // Do nothing
            }
        }
    }
    
    // Reset state when screen closes
    DisposableEffect(Unit) {
        onDispose {
            authViewModel.resetProfileUpdateState()
        }
    }
    
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
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save"
                        )
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
            // Profile image with edit capability
            Box(
                modifier = Modifier.padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Profile image
                ProfileImage(
                    imageUrl = selectedImageUri?.toString() ?: currentUser?.profilePictureUrl,
                    fallbackInitial = displayName.firstOrNull(),
                    size = 120.dp,
                    onClick = { imagePicker.launch("image/*") }
                )
                
                // Edit icon overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 0.dp, y = 0.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change Profile Picture",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Add a text prompt for changing profile picture
            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Change Profile Picture")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
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
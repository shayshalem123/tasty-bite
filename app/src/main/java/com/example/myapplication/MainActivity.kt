package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.auth.AuthViewModel
import com.example.myapplication.auth.UserViewModel
import com.example.myapplication.ui.TastyBiteApp
import com.example.myapplication.ui.screens.add.AddRecipeViewModel
import com.example.myapplication.ui.theme.TastyBiteTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.storage.FirebaseStorage
import com.example.myapplication.ui.favorites.FavoritesViewModel

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase properly with options
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        
        // Ensure we can access the custom bucket
        try {
            val storage = FirebaseStorage.getInstance("gs://tasty-bite-19b53.firebasestorage.app")
            Log.d(TAG, "Successfully connected to custom Firebase Storage bucket")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to Firebase Storage bucket", e)
        }
        
        // Initialize Auth ViewModel
        val authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        // Initialize User ViewModel
        val userViewModel = ViewModelProvider(this, UserViewModel.Factory())[UserViewModel::class.java]

        val addRecipeViewModel = ViewModelProvider(this)[AddRecipeViewModel::class.java]

        // Initialize Favorites ViewModel
        val favoritesViewModel = ViewModelProvider(this)[FavoritesViewModel::class.java]

        setContent {
            TastyBiteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TastyBiteApp(
                        authViewModel, 
                        userViewModel, 
                        addRecipeViewModel,
                        favoritesViewModel
                    )
                }
            }
        }
    }
}
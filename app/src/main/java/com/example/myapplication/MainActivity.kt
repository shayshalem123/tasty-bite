package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.auth.AuthViewModel
import com.example.myapplication.ui.TastyBiteApp
import com.example.myapplication.ui.theme.TastyBiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Auth ViewModel
        val authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        setContent {
            TastyBiteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TastyBiteApp(authViewModel)
                }
            }
        }
    }
}
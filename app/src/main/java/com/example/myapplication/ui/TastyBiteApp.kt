package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.auth.AuthState
import com.example.myapplication.auth.AuthViewModel
import com.example.myapplication.data.recommendedRecipes
import com.example.myapplication.models.Recipe
import com.example.myapplication.ui.components.BottomNavigationBar
import com.example.myapplication.ui.screens.add.AddRecipeScreen
import com.example.myapplication.ui.screens.auth.LoginScreen
import com.example.myapplication.ui.screens.auth.RegisterScreen
import com.example.myapplication.ui.screens.detail.RecipeDetailScreen
import com.example.myapplication.ui.screens.home.HomeScreen
import com.example.myapplication.ui.screens.add.AddRecipeViewModel

@Composable
fun TastyBiteApp(authViewModel: AuthViewModel) {
    // Get authState without collectAsState if it's causing issues
    var authState by remember { mutableStateOf(authViewModel.authState.value) }
    
    // Update authState when it changes
    LaunchedEffect(Unit) {
        authViewModel.authState.collect { 
            // This will update the authState
            authState = it 
        }
    }
    
    // Authentication state management
    var showLoginScreen by remember { mutableStateOf(true) }
    
    when (authState) {
        is AuthState.Initial, is AuthState.Loading -> {
            // Show loading state or splash screen
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is AuthState.Authenticated -> {
            // Show the main app content when authenticated
            AuthenticatedContent(authViewModel)
        }
        is AuthState.Unauthenticated, is AuthState.Error -> {
            // Show login or register screen
            if (showLoginScreen) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = { showLoginScreen = false }
                )
            } else {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { showLoginScreen = true }
                )
            }
        }
    }
}

@Composable
fun AuthenticatedContent(authViewModel: AuthViewModel) {
    // State to track the selected recipe
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    
    // State to track if we're adding a new recipe
    var isAddingRecipe by remember { mutableStateOf(false) }
    
    // State to hold all recipes (mutable to add new ones)
    val allRecipes = remember { mutableStateListOf<Recipe>().apply { addAll(recommendedRecipes) } }
    
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            // Show add recipe screen
            isAddingRecipe -> {
                val context = LocalContext.current
                val addRecipeViewModel = remember(context) { AddRecipeViewModel(context) }
                AddRecipeScreen(
                    onBackClick = { isAddingRecipe = false },
                    onRecipeAdded = { newRecipe -> 
                        allRecipes.add(newRecipe)
                        isAddingRecipe = false
                    },
                    authViewModel = authViewModel,
                    addRecipeViewModel = addRecipeViewModel
                )
            }
            // Show recipe detail screen
            selectedRecipe != null -> {
                RecipeDetailScreen(
                    recipe = selectedRecipe!!,
                    onBackClick = { selectedRecipe = null }
                )
            }
            // Show home screen
            else -> {
                Scaffold(
                    bottomBar = { 
                        BottomNavigationBar(
                            onLogoutClick = { authViewModel.signOut() }
                        ) 
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { isAddingRecipe = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            ),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Recipe",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    floatingActionButtonPosition = FabPosition.Center
                ) { paddingValues ->
                    HomeScreen(
                        modifier = Modifier.padding(paddingValues),
                        onRecipeClick = { recipe -> selectedRecipe = recipe },
                        recipes = allRecipes
                    )
                }
            }
        }
    }
} 
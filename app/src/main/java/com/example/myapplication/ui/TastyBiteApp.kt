package com.example.myapplication.ui

import android.util.Log
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
import com.example.myapplication.data.FirebaseFirestoreService
import com.example.myapplication.data.FirebaseStorageService
import com.example.myapplication.models.Recipe
import com.example.myapplication.ui.components.BottomNavigationBar
import com.example.myapplication.ui.screens.add.AddRecipeScreen
import com.example.myapplication.ui.screens.auth.LoginScreen
import com.example.myapplication.ui.screens.auth.RegisterScreen
import com.example.myapplication.ui.screens.detail.RecipeDetailScreen
import com.example.myapplication.ui.screens.home.HomeScreen
import com.example.myapplication.ui.screens.add.AddRecipeViewModel
import kotlinx.coroutines.launch

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
    
    // State to hold all recipes from Firestore
    val allRecipes = remember { mutableStateListOf<Recipe>() }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    
    // Create instance of Firestore service
    val firestoreService = remember { FirebaseFirestoreService() }
    
    // Create instance of Firebase Storage service
    val storageService = remember { FirebaseStorageService() }
    
    // Create a coroutine scope here - at the composable function level
    val coroutineScope = rememberCoroutineScope()
    
    // Load recipes from Firestore when the screen is first shown
    LaunchedEffect(Unit) {
        try {
            isLoading.value = true
            errorMessage.value = null
            
            firestoreService.getAllRecipes()
                .onSuccess { recipes ->
                    allRecipes.clear()
                    
                    // Process each recipe to ensure image URLs are resolved
                    recipes.forEach { recipe ->
                        // If imageUrl isn't a valid http/https URL, try to get the download URL from Firebase Storage
                        if (!recipe.imageUrl.startsWith("http://") && !recipe.imageUrl.startsWith("https://") && recipe.imageUrl.isNotEmpty()) {
                            coroutineScope.launch {
                                storageService.getImageUrl(recipe.imageUrl)
                                    .onSuccess { downloadUrl ->
                                        // Find the recipe in the allRecipes list and update its imageUrl
                                        val index = allRecipes.indexOfFirst { it.id == recipe.id }
                                        if (index >= 0) {
                                            allRecipes[index] = allRecipes[index].copy(imageUrl = downloadUrl)
                                        }
                                    }
                                    .onFailure { error ->
                                        Log.e("TastyBiteApp", "Failed to get image URL for recipe ${recipe.id}: ${error.message}")
                                    }
                            }
                        }
                        
                        // Add the recipe to the list regardless of image URL resolution
                        allRecipes.add(recipe)
                    }
                    
                    isLoading.value = false
                }
                .onFailure { error ->
                    Log.e("TastyBiteApp", "Failed to load recipes", error)
                    errorMessage.value = "Failed to load recipes: ${error.message}"
                    isLoading.value = false
                }
        } catch (e: Exception) {
            Log.e("TastyBiteApp", "Unexpected error loading recipes", e)
            errorMessage.value = "Unexpected error: ${e.message}"
            isLoading.value = false
        }
    }
    
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
                    if (isLoading.value) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (errorMessage.value != null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = errorMessage.value ?: "Unknown error",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                                Button(
                                    onClick = {
                                        // Reload recipes - using the scope defined above
                                        coroutineScope.launch {
                                            isLoading.value = true
                                            errorMessage.value = null
                                            firestoreService.getAllRecipes()
                                                .onSuccess { recipes ->
                                                    allRecipes.clear()
                                                    allRecipes.addAll(recipes)
                                                    isLoading.value = false
                                                }
                                                .onFailure { error ->
                                                    errorMessage.value = "Failed to load recipes: ${error.message}"
                                                    isLoading.value = false
                                                }
                                        }
                                    }
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    } else {
                        HomeScreen(
                            modifier = Modifier.padding(paddingValues),
                            onRecipeClick = { recipe -> selectedRecipe = recipe },
                            recipes = allRecipes
                        )
                    }
                }
            }
        }
        
        // Add a refresh action to reload recipes after adding a new one
        LaunchedEffect(isAddingRecipe) {
            if (!isAddingRecipe) {
                firestoreService.getAllRecipes()
                    .onSuccess { recipes ->
                        allRecipes.clear()
                        
                        // Process each recipe to ensure image URLs are resolved
                        recipes.forEach { recipe ->
                            // If imageUrl isn't a valid http/https URL, try to get the download URL from Firebase Storage
                            if (!recipe.imageUrl.startsWith("http://") && !recipe.imageUrl.startsWith("https://") && recipe.imageUrl.isNotEmpty()) {
                                coroutineScope.launch {
                                    storageService.getImageUrl(recipe.imageUrl)
                                        .onSuccess { downloadUrl ->
                                            // Find the recipe in the allRecipes list and update its imageUrl
                                            val index = allRecipes.indexOfFirst { it.id == recipe.id }
                                            if (index >= 0) {
                                                allRecipes[index] = allRecipes[index].copy(imageUrl = downloadUrl)
                                            }
                                        }
                                        .onFailure { error ->
                                            Log.e("TastyBiteApp", "Failed to get image URL for recipe ${recipe.id}: ${error.message}")
                                        }
                                }
                            }
                            
                            // Add the recipe to the list regardless of image URL resolution
                            allRecipes.add(recipe)
                        }
                    }
            }
        }
    }
} 
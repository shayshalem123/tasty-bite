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
import com.example.myapplication.auth.UserViewModel
import com.example.myapplication.data.RecipesDataRepository
import com.example.myapplication.data.RecipesImageRepository
import com.example.myapplication.models.Recipe
import com.example.myapplication.ui.components.BottomNavigationBar
import com.example.myapplication.ui.components.LoadingScreen
import com.example.myapplication.ui.screens.add.AddRecipeScreen
import com.example.myapplication.ui.screens.auth.AuthenticationScreen
import com.example.myapplication.ui.screens.auth.LoginScreen
import com.example.myapplication.ui.screens.auth.RegisterScreen
import com.example.myapplication.ui.screens.detail.RecipeDetailScreen
import com.example.myapplication.ui.screens.home.HomeScreen
import com.example.myapplication.ui.screens.add.AddRecipeViewModel
import com.example.myapplication.ui.screens.home.AllRecipesScreen
import com.example.myapplication.ui.screens.profile.EditProfileScreen
import com.example.myapplication.ui.screens.profile.ProfileScreen
import com.example.myapplication.ui.screens.search.SearchScreen
import com.example.myapplication.ui.screens.add.EditRecipeScreen
import com.example.myapplication.ui.screens.add.EditRecipeViewModel
import kotlinx.coroutines.launch

@Composable
fun TastyBiteApp(
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    addRecipeViewModel: AddRecipeViewModel
) {
    // Check authentication state
    val authState by authViewModel.authState.collectAsState()

    when (authState) {
        is AuthState.Initial -> {
            LoadingScreen()
        }
        is AuthState.Authenticated -> {
            AuthenticatedContent(authViewModel, userViewModel, addRecipeViewModel)
        }
        is AuthState.Unauthenticated -> {
            AuthenticationScreen(authViewModel)
        }
        is AuthState.Loading -> {
            LoadingScreen()
        }
        is AuthState.Error -> {
            val error = (authState as AuthState.Error).message
            AuthenticationScreen(authViewModel, error)
        }
    }
}

@Composable
fun AuthenticatedContent(
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    addRecipeViewModel: AddRecipeViewModel
) {
    // State to track the selected recipe
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }

    // State to track if we're adding a new recipe
    var isAddingRecipe by remember { mutableStateOf(false) }

    // State to track if we're editing a recipe
    var isEditingRecipe by remember { mutableStateOf(false) }

    // State to track if we're viewing all recipes
    var isViewingAllRecipes by remember { mutableStateOf(false) }

    // State to track if we're searching recipes
    var isSearching by remember { mutableStateOf(false) }

    // State to track if we're viewing profile
    var isViewingProfile by remember { mutableStateOf(false) }

    // State to track if we're editing profile
    var isEditingProfile by remember { mutableStateOf(false) }

    // Get the current user data
    val currentUser by authViewModel.currentUser.collectAsState()

    // State to hold all recipes from Firestore
    val allRecipes = remember { mutableStateListOf<Recipe>() }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Create instance of Firestore service
    val firestoreService = remember { RecipesDataRepository() }

    // Create instance of Firebase Storage service
    val storageService = remember { RecipesImageRepository() }

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
            isAddingRecipe -> {
                AddRecipeScreen(
                    onBackClick = { isAddingRecipe = false },
                    authViewModel = authViewModel,
                    onRecipeAdded = { recipe -> isAddingRecipe = false; allRecipes.add(recipe) },
                    addRecipeViewModel = addRecipeViewModel
                )
            }
            isEditingRecipe && selectedRecipe != null -> {
                EditRecipeScreen(
                    recipe = selectedRecipe!!,
                    onBackClick = {
                        isEditingRecipe = false
                    },
                    onRecipeUpdated = { updatedRecipe ->
                        isEditingRecipe = false

                        // Update the recipe in the list
                        val index = allRecipes.indexOfFirst { it.id == updatedRecipe.id }
                        if (index >= 0) {
                            allRecipes[index] = updatedRecipe
                        }

                        // Update the selected recipe to reflect changes
                        selectedRecipe = updatedRecipe
                    },
                    authViewModel = authViewModel
                )
            }
            selectedRecipe != null -> {
                RecipeDetailScreen(
                    recipe = selectedRecipe!!,
                    onBackClick = { selectedRecipe = null },
                    userViewModel = userViewModel,
                    authViewModel = authViewModel,
                    onEditRecipe = { recipe ->
                        isEditingRecipe = true
                    },
                    onRecipeDeleted = {
                        // Remove the recipe from the list
                        allRecipes.removeAll { it.id == selectedRecipe!!.id }

                        // Clear the selected recipe
                        selectedRecipe = null
                    }
                )
            }
            isViewingAllRecipes -> {
                AllRecipesScreen(
                    recipes = allRecipes,
                    onBackClick = { isViewingAllRecipes = false },
                    onRecipeClick = { recipe ->
                        selectedRecipe = recipe
                        isViewingAllRecipes = false
                    },
                    isLoading = isLoading.value,
                    userViewModel = userViewModel
                )
            }
            isSearching -> {
                SearchScreen(
                    recipes = allRecipes,
                    onBackClick = { isSearching = false },
                    onRecipeClick = { recipe ->
                        selectedRecipe = recipe
                        isSearching = false
                    },
                    userViewModel = userViewModel
                )
            }
            isViewingProfile -> {
                if (isEditingProfile) {
                    EditProfileScreen(
                        authViewModel = authViewModel,
                        onBackClick = { isEditingProfile = false },
                        onProfileUpdated = {
                            isEditingProfile = false
                            userViewModel.clearCache()
                        }
                    )
                } else {
                    ProfileScreen(
                        authViewModel = authViewModel,
                        onBackClick = { isViewingProfile = false },
                        onEditProfileClick = { isEditingProfile = true },
                        onRecipeClick = { recipe ->
                            selectedRecipe = recipe
                            isViewingProfile = false
                        },
                        userViewModel = userViewModel
                    )
                }
            }
            else -> {
                // Home screen with featured recipes, categories, etc.
                HomeScreen(
                    recipes = allRecipes,
                    isLoading = isLoading.value,
                    onRecipeClick = { recipe -> selectedRecipe = recipe },
                    onViewAllRecipesClick = { isViewingAllRecipes = true },
                    onAddRecipeClick = { isAddingRecipe = true },
                    onSearchClick = { isSearching = true },
                    onProfileClick = { isViewingProfile = true },
                    onSignOutClick = {
                        authViewModel.signOut()
                    },
                    userViewModel = userViewModel,
                    authViewModel = authViewModel
                )
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
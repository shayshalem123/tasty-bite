package com.example.myapplication.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Category
import com.example.myapplication.ui.components.BottomNavigationBar
import com.example.myapplication.ui.components.SearchBar
import com.example.myapplication.ui.screens.home.components.CategoriesSection
import com.example.myapplication.ui.screens.home.components.HomeHeader
import com.example.myapplication.ui.screens.home.components.RecommendationsSection
import com.example.myapplication.models.Recipe
import com.example.myapplication.data.categories
import com.example.myapplication.auth.UserViewModel
import android.util.Log
import androidx.compose.foundation.layout.offset
import com.example.myapplication.auth.AuthViewModel

@Composable
fun HomeScreen(
    recipes: List<Recipe> = emptyList(),
    isLoading: Boolean = false,
    onRecipeClick: (Recipe) -> Unit = {},
    onViewAllRecipesClick: () -> Unit = {},
    onAddRecipeClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    // Get the current authenticated user from AuthViewModel
    val currentUser by authViewModel.currentUser.collectAsState()
    
    // Get display name - use AuthViewModel's data which has the correct display name
    val displayName = currentUser?.displayName ?: "User"

    // Search state
    val searchQuery = remember { mutableStateOf("") }
    
    // Category selection state - mutable state that holds the currently selected category
    val selectedCategory = remember { mutableStateOf<Category?>(null) }
    
    // Filter recipes by search and/or category
    val filteredRecipes = if (searchQuery.value.isEmpty() && selectedCategory.value == null) {
        recipes
    } else {
        recipes.filter { recipe ->
            val matchesSearch = searchQuery.value.isEmpty() || 
                recipe.title.contains(searchQuery.value, ignoreCase = true)
            
            val matchesCategory = selectedCategory.value == null || 
                recipe.categories?.contains(selectedCategory.value?.id) == true ||
                recipe.category == selectedCategory.value?.id
                
            matchesSearch && matchesCategory
        }
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = { 
            BottomNavigationBar(
                onHomeClick = { /* Already on home */ },
                onSearchClick = onSearchClick,
                onProfileClick = onProfileClick,
                onLogoutClick = onSignOutClick
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRecipeClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                ),
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = 40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Recipe",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header with user name from Firebase Auth
            HomeHeader(name = displayName)
            
            // Search Bar
            SearchBar(
                searchQuery = searchQuery.value, 
                onSearchQueryChange = { searchQuery.value = it },
                onSearchSubmit = { onSearchClick() }
            )
            
            // Categories with selection support
            CategoriesSection(
                selectedCategory = selectedCategory.value,
                onCategorySelected = { category ->
                    // Toggle selection
                    selectedCategory.value = if (selectedCategory.value?.id == category.id) null else category
                }
            )
            
            // Show filtered recipes or all recipes
            if (searchQuery.value.isNotEmpty() || selectedCategory.value != null) {
                // Use same horizontal layout but with filtered recipes
                RecommendationsSection(
                    title = if (selectedCategory.value != null) 
                               "${selectedCategory.value?.name} Recipes" 
                            else 
                               "Search Results",
                    recipes = filteredRecipes,
                    onRecipeClick = onRecipeClick,
                    userViewModel = userViewModel,
                    isLoading = isLoading
                )
            } else {
                // Regular recommendations
                RecommendationsSection(
                    title = "All Recipes", 
                    recipes = recipes,
                    onRecipeClick = onRecipeClick,
                    onSeeAllClick = onViewAllRecipesClick,
                    userViewModel = userViewModel,
                    isLoading = isLoading
                )
            }
        }
    }
} 
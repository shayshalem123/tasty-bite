package com.example.myapplication.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.recommendedRecipes
import com.example.myapplication.models.Category
import com.example.myapplication.ui.components.SearchBar
import com.example.myapplication.ui.screens.home.components.CategoriesSection
import com.example.myapplication.ui.screens.home.components.HomeHeader
import com.example.myapplication.ui.screens.home.components.RecommendationsSection
import com.example.myapplication.models.Recipe

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onRecipeClick: (Recipe) -> Unit = {},
    recipes: List<Recipe> = recommendedRecipes
) {
    // Search state
    var searchQuery = remember { mutableStateOf("") }
    
    // Category selection for filtering
    var selectedCategory = remember { mutableStateOf<Category?>(null) }
    
    // Filtered recipes based on search and category
    val filteredRecipes = remember(searchQuery.value, selectedCategory.value, recipes) {
        recipes
            .filter { recipe ->
                // Apply search filter
                if (searchQuery.value.isNotEmpty()) {
                    (recipe.title.contains(searchQuery.value, ignoreCase = true) ||
                    recipe.author.contains(searchQuery.value, ignoreCase = true))
                } else {
                    true
                }
            }
            .filter { recipe ->
                // Apply category filter
                if (selectedCategory.value != null) {
                    recipe.categories?.contains(selectedCategory.value?.id) ?: false
                } else {
                    true
                }
            }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        HomeHeader()
        
        // Search Bar
        SearchBar(
            searchQuery = searchQuery.value, 
            onSearchQueryChange = { searchQuery.value = it }
        )
        
        // Categories with selection support
        CategoriesSection(
            selectedCategory = selectedCategory.value,
            onCategorySelected = { category ->
                // Toggle selection
                selectedCategory.value = if (selectedCategory.value?.id == category.id) null else category
            }
        )
        
        // Always use the Recommendations layout, but with different data based on filters
        if (searchQuery.value.isNotEmpty() || selectedCategory.value != null) {
            // Use same horizontal layout but with filtered recipes
            RecommendationsSection(
                title = if (selectedCategory.value != null) 
                           "${selectedCategory.value?.name} Recipes" 
                        else 
                           "Search Results",
                recipes = filteredRecipes,
                onRecipeClick = onRecipeClick
            )
        } else {
            // Regular recommendations
            RecommendationsSection(
                title = "Recommendation", 
                recipes = recipes,
                onRecipeClick = onRecipeClick
            )
        }
    }
} 
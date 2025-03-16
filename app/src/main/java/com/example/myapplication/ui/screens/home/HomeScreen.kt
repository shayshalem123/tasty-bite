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
import com.example.myapplication.ui.screens.home.components.FilteredResultsSection
import com.example.myapplication.ui.screens.home.components.HomeHeader
import com.example.myapplication.ui.screens.home.components.RecommendationsSection

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    // Search state
    var searchQuery = remember { mutableStateOf("") }
    
    // Only keep category selection for filtering
    var selectedCategory = remember { mutableStateOf<Category?>(null) }
    
    // Combined filtering logic - simplified to use only search and category
    val filteredRecipes = remember(searchQuery.value, selectedCategory.value) {
        recommendedRecipes
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
        
        // Search Bar - simplified without filter button
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
        
        // Show search results or filtered content
        if (searchQuery.value.isNotEmpty() || selectedCategory.value != null) {
            // Show filtered results
            FilteredResultsSection(recipes = filteredRecipes)
        } else {
            // Show normal content when not filtering
            // Recommendations
            RecommendationsSection()
        }
    }
} 
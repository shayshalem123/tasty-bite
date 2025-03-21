package com.example.myapplication.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FavoritesRepository
import com.example.myapplication.data.RecipesDataRepository
import com.example.myapplication.models.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {
    private val favoritesRepository = FavoritesRepository()
    private val recipesRepository = RecipesDataRepository()
    
    // State for favorite recipes
    private val _favoriteRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val favoriteRecipes: StateFlow<List<Recipe>> = _favoriteRecipes
    
    // State for loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // State for current recipe favorite status
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite
    
    // Load all favorite recipes for a user
    fun loadFavorites(userEmail: String) {
        if (userEmail.isEmpty()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            
            // Get favorite recipe IDs
            favoritesRepository.getUserFavoriteIds(userEmail)
                .onSuccess { favoriteIds ->
                    if (favoriteIds.isEmpty()) {
                        _favoriteRecipes.value = emptyList()
                        _isLoading.value = false
                        return@launch
                    }
                    
                    // Load all recipes and filter for favorites
                    recipesRepository.getAllRecipes()
                        .onSuccess { allRecipes ->
                            _favoriteRecipes.value = allRecipes.filter { it.id in favoriteIds }
                            _isLoading.value = false
                        }
                        .onFailure {
                            _isLoading.value = false
                        }
                }
                .onFailure {
                    _isLoading.value = false
                }
        }
    }
    
    // Check if a recipe is a favorite
    fun checkFavoriteStatus(userEmail: String, recipeId: String) {
        if (userEmail.isEmpty() || recipeId.isEmpty()) return
        
        viewModelScope.launch {
            favoritesRepository.isRecipeFavorite(userEmail, recipeId)
                .onSuccess { isFavorite ->
                    _isFavorite.value = isFavorite
                }
        }
    }
    
    // Toggle favorite status
    fun toggleFavorite(userEmail: String, recipeId: String) {
        if (userEmail.isEmpty() || recipeId.isEmpty()) return
        
        viewModelScope.launch {
            if (_isFavorite.value) {
                // Remove from favorites
                favoritesRepository.removeFavorite(userEmail, recipeId)
                    .onSuccess {
                        _isFavorite.value = false
                        // Refresh favorites list
                        loadFavorites(userEmail)
                    }
            } else {
                // Add to favorites
                favoritesRepository.addFavorite(userEmail, recipeId)
                    .onSuccess {
                        _isFavorite.value = true
                        // Refresh favorites list
                        loadFavorites(userEmail)
                    }
            }
        }
    }
    
    // Add this reset method
    fun resetFavoriteState() {
        _isFavorite.value = false
    }
} 
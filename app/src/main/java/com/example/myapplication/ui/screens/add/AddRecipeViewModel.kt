package com.example.myapplication.ui.screens.add

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FirebaseFirestoreService
import com.example.myapplication.data.FirebaseRecipeService
import com.example.myapplication.models.Recipe
import com.example.myapplication.utils.FirebaseStorageUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddRecipeViewModel(private val context: Context) : ViewModel() {
    private val TAG = "AddRecipeViewModel"
    private val firebaseRecipeService = FirebaseRecipeService(context)
    private val firestoreService = FirebaseFirestoreService()
    
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Initial)
    val saveState: StateFlow<SaveState> = _saveState

    private val _debug = MutableStateFlow<String?>(null)
    val debug: StateFlow<String?> = _debug
    
    fun saveRecipe(recipe: Recipe, onSuccess: (Recipe) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting recipe save for: ${recipe.title}")
                _saveState.value = SaveState.Saving
                _debug.value = "Saving recipe to Firebase..."
                
                // First save to Storage
                var storageUrl: String? = null
                firebaseRecipeService.uploadRecipeToStorage(recipe)
                    .onSuccess { url ->
                        storageUrl = url
                        _debug.value = "Recipe JSON uploaded to Storage"
                    }
                    .onFailure { error ->
                        Log.w(TAG, "Storage upload failed, continuing with Firestore only", error)
                    }

                // Then save to Firestore database
                firestoreService.saveRecipe(recipe, storageUrl)
                    .onSuccess { recipeId ->
                        // Update the recipe with the generated ID
                        val savedRecipe = recipe.copy(id = recipeId)
                        _saveState.value = SaveState.Success
                        _debug.value = "Recipe saved successfully with ID: $recipeId"
                        Log.d(TAG, "Recipe saved successfully with ID: $recipeId")
                        onSuccess(savedRecipe)
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Failed to save recipe to Firestore", error)
                        _debug.value = "Failed to save recipe: ${error.message}"
                        _saveState.value = SaveState.Error(error.message ?: "Failed to save recipe")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error saving recipe", e)
                _debug.value = "Unexpected error: ${e.message}"
                _saveState.value = SaveState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }
    
    fun testStorageConnection() {
        viewModelScope.launch {
            _debug.value = "Testing Storage connection..."
            FirebaseStorageUtil.testConnection()
                .onSuccess {
                    _debug.value = "Storage connection successful!"
                }
                .onFailure { error ->
                    _debug.value = "Storage connection failed: ${error.message}"
                }
        }
    }
}

sealed class SaveState {
    object Initial : SaveState()
    object Saving : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
} 
package com.example.myapplication.ui.screens.add

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.RecipesDataRepository
import com.example.myapplication.data.RecipesImageRepository
import com.example.myapplication.models.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddRecipeViewModel() : ViewModel() {
    private val TAG = "RecipeViewModel"

    private val firestoreService = RecipesDataRepository()
    private val storageService = RecipesImageRepository()
    
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Initial)
    val saveState: StateFlow<SaveState> = _saveState

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState
    
    // Recipe being edited
    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe: StateFlow<Recipe?> = _recipe
    
    // Whether we're in edit mode
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode
    
    // Set the recipe to be edited
    fun setRecipeToEdit(recipe: Recipe?) {
        resetAllState() // Reset state first
        _recipe.value = recipe
        _isEditMode.value = recipe != null
    }
    
    fun resetSaveState() {
        _saveState.value = SaveState.Initial
    }
    
    // Reset all state in the ViewModel
    fun resetAllState() {
        _saveState.value = SaveState.Initial
        _uploadState.value = UploadState.Idle
        _recipe.value = null
        _isEditMode.value = false
    }

    fun saveRecipe(recipe: Recipe, imageUri: Uri?, onSuccess: (Recipe) -> Unit) {
        viewModelScope.launch {
            try {
                _saveState.value = SaveState.Saving
                
                // Log recipe categories for debugging
                Log.d(TAG, "Saving recipe with categories: ${recipe.categories}")
                
                val processedImageUri = imageUri ?: return@launch
                val imagePath = storageService.uploadImage(processedImageUri).getOrThrow()

                val savedRecipe = firestoreService.saveRecipe(
                    recipe = recipe,
                    imagePath = imagePath,
                ).getOrThrow()

                _saveState.value = SaveState.Success
                Log.d(TAG, "Recipe saved successfully with ID: ${savedRecipe.id}")
                Log.d(TAG, "Saved recipe has categories: ${savedRecipe.categories}")

                onSuccess(savedRecipe)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error saving recipe", e)
                _saveState.value = SaveState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }
    
    fun updateRecipe(recipe: Recipe, imageUri: Uri?, onSuccess: (Recipe) -> Unit) {
        viewModelScope.launch {
            try {
                _saveState.value = SaveState.Saving
                
                // If there's a new image, upload it
                val newImagePath = if (imageUri != null) {
                    try {
                        _uploadState.value = UploadState.Uploading
                        val path = storageService.uploadImage(imageUri).getOrThrow()
                        _uploadState.value = UploadState.Idle
                        path
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to upload image", e)
                        _uploadState.value = UploadState.Error("Failed to upload image: ${e.message}")
                        null
                    }
                } else {
                    null
                }

                val updatedRecipe = firestoreService.updateRecipe(
                    recipe = recipe,
                    newImagePath = newImagePath
                ).getOrThrow()

                _saveState.value = SaveState.Success
                Log.d(TAG, "Recipe updated successfully with ID: ${updatedRecipe.id}")

                onSuccess(updatedRecipe)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error updating recipe", e)
                _saveState.value = SaveState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }
}
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
    private val TAG = "AddRecipeViewModel"

    private val firestoreService = RecipesDataRepository()
    private val storageService = RecipesImageRepository()
    
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Initial)
    val saveState: StateFlow<SaveState> = _saveState

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState
    
    fun resetSaveState() {
        _saveState.value = SaveState.Initial
    }

    fun saveRecipe(recipe: Recipe, imageUri: Uri, onSuccess: (Recipe) -> Unit) {
        viewModelScope.launch {
            try {
                _saveState.value = SaveState.Saving

                val imagePath = storageService.uploadImage(imageUri).getOrThrow()

                val savedRecipe = firestoreService.saveRecipe(
                    recipe = recipe,
                    imagePath = imagePath,
                ).getOrThrow();

                _saveState.value = SaveState.Success
                Log.d(TAG, "Recipe saved successfully with ID: ${savedRecipe.id}")

                onSuccess(savedRecipe)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error saving recipe", e)
                _saveState.value = SaveState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }
}
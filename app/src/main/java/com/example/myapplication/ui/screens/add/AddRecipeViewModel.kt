package com.example.myapplication.ui.screens.add

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FirebaseFirestoreService
import com.example.myapplication.data.FirebaseRecipeService
import com.example.myapplication.data.FirebaseStorageService
import com.example.myapplication.models.Recipe
import com.example.myapplication.utils.FirebaseStorageUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddRecipeViewModel(private val context: Context) : ViewModel() {
    private val TAG = "AddRecipeViewModel"
    private val firebaseRecipeService = FirebaseRecipeService(context)
    private val firestoreService = FirebaseFirestoreService()
    private val storageService = FirebaseStorageService()
    
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Initial)
    val saveState: StateFlow<SaveState> = _saveState

    private val _debug = MutableStateFlow<String?>(null)
    val debug: StateFlow<String?> = _debug
    
    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress: StateFlow<Int> = _uploadProgress

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState
    
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

    suspend fun uploadImage(context: Context, imageUri: Uri): String? {
        _uploadState.value = UploadState.Loading
        
        storageService.uploadImage(
            context = context,
            imageUri = imageUri,
            onProgress = { progress ->
                _uploadProgress.value = progress
            }
        ).fold(
            onSuccess = { downloadUrl ->
                _uploadState.value = UploadState.Success(downloadUrl)
                return downloadUrl
            },
            onFailure = { exception ->
                _uploadState.value = UploadState.Error(exception.message ?: "Unknown error")
                return null
            }
        )
    }

    fun cancelUpload() {
        storageService.cancelUpload()
        _uploadProgress.value = 0
        _uploadState.value = UploadState.Idle
    }
}

sealed class SaveState {
    object Initial : SaveState()
    object Saving : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Success(val imageUrl: String) : UploadState()
    data class Error(val message: String) : UploadState()
} 
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

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
    
    fun resetSaveState() {
        _saveState.value = SaveState.Initial
    }
    
    /**
     * Saves a recipe with an image upload
     * Image must be provided as it's a required field
     *
     * @param recipe The recipe object to save
     * @param imageUri The URI of the image to upload (required)
     * @param onSuccess Callback called when recipe is saved successfully
     */
    fun saveRecipe(recipe: Recipe, imageUri: Uri, onSuccess: (Recipe) -> Unit) {
        viewModelScope.launch {
            try {
                _saveState.value = SaveState.Saving
                _debug.value = "Saving recipe to Firebase..."
                
                firestoreService.saveRecipeWithImage(
                    recipe = recipe,
                    imageUri = imageUri,
                    context = context,
                    onProgressUpdate = { progress ->
                        _uploadProgress.value = progress
                    }
                ).fold(
                    onSuccess = { savedRecipe ->
                        _saveState.value = SaveState.Success
                        _debug.value = "Recipe saved successfully with ID: ${savedRecipe.id}"
                        Log.d(TAG, "Recipe saved successfully with ID: ${savedRecipe.id}")
                        onSuccess(savedRecipe)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to save recipe", error)
                        _debug.value = "Failed to save recipe: ${error.message}"
                        _saveState.value = SaveState.Error(error.message ?: "Failed to save recipe")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error saving recipe", e)
                _debug.value = "Unexpected error: ${e.message}"
                _saveState.value = SaveState.Error("An unexpected error occurred: ${e.message}")
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

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Success(val imageUrl: String) : UploadState()
    data class Error(val message: String) : UploadState()
}
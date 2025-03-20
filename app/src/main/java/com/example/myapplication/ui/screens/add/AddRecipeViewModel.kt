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
    
    fun setSaving() {
        _saveState.value = SaveState.Saving
    }
    
    fun setError(message: String) {
        _saveState.value = SaveState.Error(message)
    }
    
    fun saveRecipe(recipe: Recipe, onSuccess: (Recipe) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting recipe save for: ${recipe.title}")
                _debug.value = "Saving recipe to Firebase..."
                _saveState.value = SaveState.Saving
                
                // Generate a unique ID if one doesn't exist
                val recipeId = if (recipe.id.isNullOrEmpty()) {
                    UUID.randomUUID().toString()
                } else {
                    recipe.id
                }
                
                // 1. Process image: If imageUrl is a URI, upload it to Storage first
                var finalImageUrl = recipe.imageUrl
                if (finalImageUrl.startsWith("content://")) {
                    _debug.value = "Uploading image to Firebase Storage..."
                    try {
                        val uri = Uri.parse(finalImageUrl)
                        finalImageUrl = storageService.uploadImage(
                            context = context,
                            imageUri = uri,
                            onProgress = { progress ->
                                _uploadProgress.value = progress
                            }
                        ).fold(
                            onSuccess = { url -> 
                                _debug.value = "Image uploaded successfully"
                                url 
                            },
                            onFailure = { error ->
                                Log.e(TAG, "Failed to upload image", error)
                                _debug.value = "Image upload failed, using original URL"
                                finalImageUrl
                            }
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error uploading image", e)
                        _debug.value = "Error uploading image: ${e.message}"
                        // Continue with the original URL if upload fails
                    }
                }
                
                // 2. Now save the complete recipe data to Firestore
                val recipeWithId = recipe.copy(id = recipeId, imageUrl = finalImageUrl)
                
                val db = Firebase.firestore
                
                // Convert ingredients to a format Firestore can store
                val ingredientsData = recipeWithId.ingredients?.map { ingredient ->
                    mapOf(
                        "name" to ingredient.name,
                        "amount" to ingredient.amount,
                        "imageUrl" to (ingredient.imageUrl ?: 0).toString()
                    )
                } ?: listOf()
                
                // Create a map of the recipe data
                val recipeData = hashMapOf(
                    "id" to recipeId,
                    "title" to recipeWithId.title,
                    "author" to recipeWithId.author,
                    "imageUrl" to finalImageUrl,
                    "description" to (recipeWithId.description ?: ""),
                    "cookingTime" to (recipeWithId.cookingTime ?: ""),
                    "difficulty" to (recipeWithId.difficulty ?: ""),
                    "calories" to (recipeWithId.calories ?: ""),
                    "ingredients" to ingredientsData,
                    "categories" to (recipeWithId.categories ?: if (recipeWithId.category.isNotEmpty()) listOf(recipeWithId.category) else listOf()),
                    "instructions" to (recipeWithId.instructions ?: listOf<String>()),
                    "cookTime" to recipeWithId.cookTime,
                    "servings" to recipeWithId.servings,
                    "category" to recipeWithId.category,
                    "isFavorite" to recipeWithId.isFavorite,
                    "createdBy" to recipeWithId.createdBy,
                    "createdAt" to System.currentTimeMillis()
                )
                
                try {
                    _debug.value = "Saving recipe to Firestore..."
                    // Save to the "recipes" collection
                    db.collection("recipes").document(recipeId).set(recipeData).await()
                    
                    _saveState.value = SaveState.Success
                    _debug.value = "Recipe saved successfully with ID: $recipeId"
                    Log.d(TAG, "Recipe saved successfully with ID: $recipeId")
                    onSuccess(recipeWithId)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save recipe to Firestore", e)
                    _debug.value = "Failed to save recipe: ${e.message}"
                    _saveState.value = SaveState.Error(e.message ?: "Failed to save recipe")
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
        _uploadProgress.value = 0
        
        try {
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
                    Log.e(TAG, "Image upload failed", exception)
                    _uploadState.value = UploadState.Error(exception.message ?: "Unknown error")
                    return null
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during image upload", e)
            _uploadState.value = UploadState.Error("Upload failed: ${e.message ?: "Unknown error"}")
            return null
        }
        
        // This is a fallback in case something goes wrong with the fold operation
        return null
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
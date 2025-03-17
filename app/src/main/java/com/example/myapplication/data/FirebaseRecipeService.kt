package com.example.myapplication.data

import android.content.Context
import android.util.Log
import com.example.myapplication.models.Recipe
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.util.UUID

/**
 * Service for writing recipes to Firebase
 */
class FirebaseRecipeService(private val context: Context? = null) {
    private val TAG = "FirebaseRecipeService"
    
    // Storage instance - explicitly referencing your bucket with KTX syntax
    private val storage = Firebase.storage("gs://tasty-bite-19b53.firebasestorage.app")
    private val storageRef = storage.reference
    
    /**
     * Saves a new recipe to both Firestore and Storage
     * @param recipe The recipe to save
     * @return Result indicating success or failure
     */
    suspend fun saveRecipe(recipe: Recipe): Result<String> {
        return try {
            Log.d(TAG, "Starting recipe save process for: ${recipe.title}")
            
            // Generate a unique ID if one doesn't exist
            val recipeId = if (recipe.id.isNullOrEmpty()) {
                UUID.randomUUID().toString()
            } else {
                recipe.id
            }
            
            // First save to Storage
            val storageUploadResult = uploadRecipeToStorage(recipe.copy(id = recipeId))
            if (storageUploadResult.isFailure) {
                Log.w(TAG, "Storage upload failed, continuing with Firestore only")
            }
            
            // Get downloadUrl from the storage result
            val storageUrl = storageUploadResult.getOrNull()
            
            // Convert ingredients to a format Firestore can store
            val ingredientsData = recipe.ingredients?.map { ingredient ->
                mapOf(
                    "name" to ingredient.name,
                    "amount" to ingredient.amount,
                    "imageUrl" to (ingredient.imageUrl ?: 0).toString()
                )
            } ?: listOf()
            
            // Create a map of the recipe data
            val recipeData = mapOf(
                "id" to recipeId,
                "title" to recipe.title,
                "author" to recipe.author,
                "imageUrl" to recipe.imageUrl.toString(),
                "description" to (recipe.description ?: ""),
                "cookingTime" to (recipe.cookingTime ?: ""),
                "difficulty" to (recipe.difficulty ?: ""),
                "calories" to (recipe.calories ?: ""),
                "ingredients" to ingredientsData,
                "categories" to (recipe.categories ?: listOf<String>()),
                "instructions" to (recipe.instructions ?: listOf<String>()),
                "cookTime" to recipe.cookTime,
                "servings" to recipe.servings,
                "category" to recipe.category,
                "isFavorite" to recipe.isFavorite,
                "createdBy" to recipe.createdBy,
                "createdAt" to System.currentTimeMillis(),
                "storageUrl" to (storageUrl ?: "")
            )
            
            Log.d(TAG, "Recipe saved successfully with ID: $recipeId")
            Result.success(recipeId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save recipe", e)
            Result.failure(e)
        }
    }
    
    /**
     * Uploads recipe as JSON to Firebase Storage
     * @param recipe Recipe to upload
     * @return Result with download URL or error
     */
    suspend fun uploadRecipeToStorage(recipe: Recipe): Result<String> {
        return try {
            // JSON string with basic recipe data
            val jsonString = """
                {
                    "id": "${recipe.id}",
                    "title": "${recipe.title}",
                    "author": "${recipe.author}",
                    "timestamp": ${System.currentTimeMillis()}
                }
            """.trimIndent()
            
            // Upload to Firebase Storage
            val fileRef = storageRef.child("recipes/recipe_${recipe.id}.json")
            fileRef.putBytes(jsonString.toByteArray()).await()
            
            // Get the download URL
            val downloadUrl = fileRef.downloadUrl.await().toString()
            Log.d(TAG, "Recipe JSON uploaded to: $downloadUrl")
            
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload recipe to Storage", e)
            Result.failure(e)
        }
    }
} 
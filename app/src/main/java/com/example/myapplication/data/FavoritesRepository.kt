package com.example.myapplication.data

import com.example.myapplication.models.Recipe
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoritesRepository {
    private val firestore = FirebaseFirestore.getInstance("tasty-bite")
    private val favoritesCollection = firestore.collection("favorites")
    
    // Get all favorite recipe IDs for a user
    suspend fun getUserFavoriteIds(userEmail: String): Result<List<String>> {
        return try {
            val document = favoritesCollection.document(userEmail).get().await()
            
            if (document.exists()) {
                @Suppress("UNCHECKED_CAST")
                val favorites = document.get("favoriteRecipes") as? List<String> ?: emptyList()
                Result.success(favorites)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Add a recipe to favorites
    suspend fun addFavorite(userEmail: String, recipeId: String): Result<Unit> {
        return try {
            val docRef = favoritesCollection.document(userEmail)
            val document = docRef.get().await()
            
            if (document.exists()) {
                // Update existing document
                docRef.update("favoriteRecipes", FieldValue.arrayUnion(recipeId)).await()
            } else {
                // Create new document
                val favorites = mapOf("favoriteRecipes" to listOf(recipeId))
                docRef.set(favorites).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Remove a recipe from favorites
    suspend fun removeFavorite(userEmail: String, recipeId: String): Result<Unit> {
        return try {
            val docRef = favoritesCollection.document(userEmail)
            docRef.update("favoriteRecipes", FieldValue.arrayRemove(recipeId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Check if a recipe is a favorite
    suspend fun isRecipeFavorite(userEmail: String, recipeId: String): Result<Boolean> {
        return try {
            val favorites = getUserFavoriteIds(userEmail).getOrDefault(emptyList())
            Result.success(recipeId in favorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 
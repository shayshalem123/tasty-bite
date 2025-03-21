package com.example.myapplication.data

import android.net.Uri
import android.util.Log
import com.example.myapplication.models.Recipe
import com.example.myapplication.models.Ingredient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Service for managing recipes in Firebase Firestore database
 */
class RecipesDataRepository {
    private val TAG = "FirebaseFirestoreService"

    private val db = FirebaseFirestore.getInstance("tasty-bite")
    private val recipesCollection = db.collection("recipes")

    suspend fun saveRecipe(
        recipe: Recipe, 
        imagePath: String,
    ): Result<Recipe> {
        return try {
            Log.d(TAG, "Starting recipe save with image for: ${recipe.title}")
            
            // Generate a unique ID if one doesn't exist
            val recipeId = if (recipe.id.isBlank()) {
                UUID.randomUUID().toString()
            } else {
                recipe.id
            }

            val ingredientsData = recipe.ingredients?.map { ingredient ->
                mapOf(
                    "name" to ingredient.name,
                    "amount" to ingredient.amount,
                    "imageUrl" to (ingredient.imageUrl ?: 0).toString()
                )
            } ?: listOf()

            val recipeData = hashMapOf(
                "id" to recipeId,
                "title" to recipe.title,
                "imageUrl" to imagePath,
                "description" to (recipe.description ?: ""),
                "cookingTime" to (recipe.cookingTime ?: ""),
                "difficulty" to (recipe.difficulty ?: ""),
                "calories" to (recipe.calories ?: ""),
                "ingredients" to ingredientsData,
                "categories" to (recipe.categories?.takeIf { it.isNotEmpty() } ?: 
                    if (recipe.category.isNotEmpty()) listOf(recipe.category) else listOf()),
                "instructions" to recipe.instructions,
                "cookTime" to recipe.cookTime,
                "servings" to recipe.servings,
                "category" to recipe.category,
                "isFavorite" to recipe.isFavorite,
                "createdBy" to recipe.createdBy,
                "createdAt" to System.currentTimeMillis()
            )
            
            Log.d(TAG, "Storing recipe with categories: ${recipe.categories}, main category: ${recipe.category}")
            Log.d(TAG, "Categories stored in Firestore: ${recipeData["categories"]}")

            recipesCollection.document(recipeId).set(recipeData).await()

            val savedRecipe = recipe.copy(id = recipeId, imageUrl = imagePath)
            Log.d(TAG, "Recipe saved successfully with ID: $recipeId")
            
            Result.success(savedRecipe)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save recipe to Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun getAllRecipes(): Result<List<Recipe>> {
        return try {
            Log.d(TAG, "Fetching all recipes from Firestore")
            db.clearPersistence() // Clears all cached data

            
            // Return empty list if there's an issue with connectivity
            try {
                val snapshot = recipesCollection.get().await()
                
                val recipes = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        
                        // Extract ingredients
                        val ingredientsList = (data["ingredients"] as? List<Map<String, Any>>)?.map { ingredientData ->
                            val name = ingredientData["name"] as? String ?: ""
                            val amount = ingredientData["amount"] as? String ?: ""
                            val imageUrl = (ingredientData["imageUrl"] as? String)?.toIntOrNull() ?: 0
                            
                            Ingredient(name, amount, imageUrl)
                        } ?: listOf()
                        
                        // Extract categories
                        val categories = try {
                            (data["categories"] as? List<*>)?.filterIsInstance<String>() ?: listOf()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing categories", e)
                            listOf()
                        }
                        
                        Log.d(TAG, "Retrieved categories for ${data["title"]}: $categories")
                        
                        // Extract basic fields
                        Recipe(
                            id = data["id"] as? String ?: doc.id,
                            title = data["title"] as? String ?: "",
                            imageUrl = data["imageUrl"] as? String ?: "",
                            description = data["description"] as? String,
                            cookingTime = data["cookingTime"] as? String,
                            difficulty = data["difficulty"] as? String,
                            calories = data["calories"] as? String,
                            ingredients = ingredientsList,
                            categories = categories,
                            instructions = (data["instructions"] as? List<*>)?.filterIsInstance<String>() ?: listOf(),
                            cookTime = (data["cookTime"] as? Number)?.toInt() ?: 0,
                            servings = (data["servings"] as? Number)?.toInt() ?: 0,
                            category = data["category"] as? String ?: "",
                            isFavorite = data["isFavorite"] as? Boolean ?: false,
                            createdBy = data["createdBy"] as? String ?: "",
                            createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing recipe document: ${doc.id}", e)
                        null
                    }
                }

                Log.d(TAG, "Successfully fetched ${recipes.size} recipes")
                Result.success(recipes)
            } catch (e: Exception) {
                Log.e(TAG, "Error in data fetch, returning empty list", e)
                Result.success(emptyList())  // Return empty list instead of failure to prevent app crash
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch recipes from Firestore", e)
            Result.success(emptyList())  // Return empty list instead of failure
        }
    }
    
    suspend fun getUserRecipes(userEmail: String): Result<List<Recipe>> {
        return try {
            Log.d(TAG, "Fetching recipes for user: $userEmail")
            
            try {
                val snapshot = recipesCollection
                    .whereEqualTo("createdBy", userEmail)
                    .get()
                    .await()
                
                val recipes = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        
                        // Extract ingredients
                        val ingredientsList = (data["ingredients"] as? List<Map<String, Any>>)?.map { ingredientData ->
                            val name = ingredientData["name"] as? String ?: ""
                            val amount = ingredientData["amount"] as? String ?: ""
                            val imageUrl = (ingredientData["imageUrl"] as? String)?.toIntOrNull() ?: 0
                            
                            Ingredient(name, amount, imageUrl)
                        } ?: listOf()
                        
                        // Extract categories
                        val categories = try {
                            (data["categories"] as? List<*>)?.filterIsInstance<String>() ?: listOf()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing categories", e)
                            listOf()
                        }
                        
                        Log.d(TAG, "Retrieved categories for ${data["title"]}: $categories")
                        
                        // Extract basic fields
                        Recipe(
                            id = data["id"] as? String ?: doc.id,
                            title = data["title"] as? String ?: "",
                            imageUrl = data["imageUrl"] as? String ?: "",
                            description = data["description"] as? String,
                            cookingTime = data["cookingTime"] as? String,
                            difficulty = data["difficulty"] as? String,
                            calories = data["calories"] as? String,
                            ingredients = ingredientsList,
                            categories = categories,
                            instructions = (data["instructions"] as? List<*>)?.filterIsInstance<String>() ?: listOf(),
                            cookTime = (data["cookTime"] as? Number)?.toInt() ?: 0,
                            servings = (data["servings"] as? Number)?.toInt() ?: 0,
                            category = data["category"] as? String ?: "",
                            isFavorite = data["isFavorite"] as? Boolean ?: false,
                            createdBy = data["createdBy"] as? String ?: "",
                            createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing recipe document: ${doc.id}", e)
                        null
                    }
                }

                Log.d(TAG, "Successfully fetched ${recipes.size} recipes for user: $userEmail")
                Result.success(recipes)
            } catch (e: Exception) {
                Log.e(TAG, "Error in data fetch for user recipes, returning empty list", e)
                Result.success(emptyList())
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch user recipes from Firestore", e)
            Result.success(emptyList())
        }
    }
    
    suspend fun deleteRecipe(recipeId: String): Result<Boolean> {
        return try {
            Log.d(TAG, "Deleting recipe with ID: $recipeId")
            
            // Delete the recipe document from Firestore
            recipesCollection.document(recipeId).delete().await()
            
            Log.d(TAG, "Recipe deleted successfully with ID: $recipeId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete recipe from Firestore", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateRecipe(
        recipe: Recipe,
        newImagePath: String? = null
    ): Result<Recipe> {
        return try {
            Log.d(TAG, "Updating recipe with ID: ${recipe.id}")
            
            val ingredientsData = recipe.ingredients?.map { ingredient ->
                mapOf(
                    "name" to ingredient.name,
                    "amount" to ingredient.amount,
                    "imageUrl" to (ingredient.imageUrl ?: 0).toString()
                )
            } ?: listOf()
            
            val recipeData = hashMapOf(
                "id" to recipe.id,
                "title" to recipe.title,
                "imageUrl" to (newImagePath ?: recipe.imageUrl),
                "description" to (recipe.description ?: ""),
                "cookingTime" to (recipe.cookingTime ?: ""),
                "difficulty" to (recipe.difficulty ?: ""),
                "calories" to (recipe.calories ?: ""),
                "ingredients" to ingredientsData,
                "categories" to (recipe.categories ?: if (recipe.category.isNotEmpty()) listOf(recipe.category) else listOf()),
                "instructions" to recipe.instructions,
                "cookTime" to recipe.cookTime,
                "servings" to recipe.servings,
                "category" to recipe.category,
                "isFavorite" to recipe.isFavorite,
                "createdBy" to recipe.createdBy,
                "createdAt" to recipe.createdAt
            )
            
            // Update the recipe document in Firestore
            recipesCollection.document(recipe.id).set(recipeData).await()
            
            val updatedRecipe = recipe.copy(imageUrl = newImagePath ?: recipe.imageUrl)
            Log.d(TAG, "Recipe updated successfully with ID: ${recipe.id}")
            
            Result.success(updatedRecipe)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update recipe in Firestore", e)
            Result.failure(e)
        }
    }
} 
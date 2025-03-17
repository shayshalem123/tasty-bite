package com.example.myapplication.data

import android.util.Log
import com.example.myapplication.models.Recipe
import com.example.myapplication.models.Ingredient
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Service for managing recipes in Firebase Firestore database
 * Using KTX syntax for cleaner Kotlin integration
 */
class FirebaseFirestoreService {
    private val TAG = "FirebaseFirestoreService"
    
    // Initialize Firestore with KTX syntax
    private val db by lazy { 
        try {
            Firebase.firestore
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firestore", e)
            throw e
        }
    }
    
    // Reference to recipes collection
    private val recipesCollection by lazy { db.collection("recipes") }
    
    /**
     * Saves a recipe to Firestore database using coroutines
     * @param recipe The recipe to save
     * @param storageUrl Optional URL to the recipe's storage JSON (if available)
     * @return Result containing the recipe ID or error
     */
    suspend fun saveRecipe(recipe: Recipe, storageUrl: String? = null): Result<String> {
        return try {
            Log.d(TAG, "Saving recipe to Firestore: ${recipe.title}")
            
            // Generate a unique ID if one doesn't exist
            val recipeId = if (recipe.id.isBlank()) {
                UUID.randomUUID().toString()
            } else {
                recipe.id
            }
            
            // Convert ingredients to a format Firestore can store
            val ingredientsData = recipe.ingredients?.map { ingredient ->
                mapOf(
                    "name" to ingredient.name,
                    "amount" to ingredient.amount,
                    "imageUrl" to (ingredient.imageUrl ?: 0).toString()
                )
            } ?: listOf()
            
            // Create a map of the recipe data
            val recipeData = hashMapOf(
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

            recipesCollection.document(recipeId).set(recipeData).await()

            Log.d(TAG, "Recipe saved successfully to Firestore with ID: $recipeId")
            Result.success(recipeId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save recipe to Firestore", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all recipes from Firestore
     * @return Result containing a list of recipes or error
     */
    suspend fun getAllRecipes(): Result<List<Recipe>> {
        return try {
            Log.d(TAG, "Fetching all recipes from Firestore")
            
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
                        
                        // Extract basic fields
                        Recipe(
                            id = data["id"] as? String ?: doc.id,
                            title = data["title"] as? String ?: "",
                            author = data["author"] as? String ?: "",
                            imageUrl = (data["imageUrl"] as? String)?.toIntOrNull() ?: 0,
                            description = data["description"] as? String,
                            cookingTime = data["cookingTime"] as? String,
                            difficulty = data["difficulty"] as? String,
                            calories = data["calories"] as? String,
                            ingredients = ingredientsList,
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
    
    /**
     * Get a single recipe by ID
     * @param recipeId The ID of the recipe to fetch
     * @return Result containing the recipe or error
     */
    suspend fun getRecipe(recipeId: String): Result<Recipe> {
        return try {
            Log.d(TAG, "Fetching recipe with ID: $recipeId")
            
            val docSnapshot = recipesCollection.document(recipeId).get().await()
            
            if (!docSnapshot.exists()) {
                return Result.failure(Exception("Recipe not found"))
            }
            
            val data = docSnapshot.data ?: return Result.failure(Exception("Recipe data is null"))
            
            // Extract ingredients
            val ingredientsList = (data["ingredients"] as? List<Map<String, Any>>)?.map { ingredientData ->
                val name = ingredientData["name"] as? String ?: ""
                val amount = ingredientData["amount"] as? String ?: ""
                val imageUrl = (ingredientData["imageUrl"] as? String)?.toIntOrNull() ?: 0
                
                Ingredient(name, amount, imageUrl)
            } ?: listOf()
            
            // Extract basic fields
            val recipe = Recipe(
                id = data["id"] as? String ?: docSnapshot.id,
                title = data["title"] as? String ?: "",
                author = data["author"] as? String ?: "",
                imageUrl = (data["imageUrl"] as? String)?.toIntOrNull() ?: 0,
                description = data["description"] as? String,
                cookingTime = data["cookingTime"] as? String,
                difficulty = data["difficulty"] as? String,
                calories = data["calories"] as? String,
                ingredients = ingredientsList,
                instructions = (data["instructions"] as? List<*>)?.filterIsInstance<String>() ?: listOf(),
                cookTime = (data["cookTime"] as? Number)?.toInt() ?: 0,
                servings = (data["servings"] as? Number)?.toInt() ?: 0,
                category = data["category"] as? String ?: "",
                isFavorite = data["isFavorite"] as? Boolean ?: false,
                createdBy = data["createdBy"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0
            )
            
            Log.d(TAG, "Successfully fetched recipe: ${recipe.title}")
            Result.success(recipe)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch recipe", e)
            Result.failure(e)
        }
    }
} 
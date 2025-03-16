package com.example.myapplication.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Recipe
import com.example.myapplication.ui.screens.detail.components.*

@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Recipe Image with navigation
            RecipeImageHeader(
                imageResId = recipe.imageUrl,
                title = recipe.title,
                onBackClick = onBackClick
            )
            
            // Recipe Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .offset(y = (-20).dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                // Recipe header with title, author, rating, stats
                RecipeHeader(
                    title = recipe.title,
                    author = recipe.author,
                    cookingTime = recipe.cookingTime ?: "10 mins",
                    difficulty = recipe.difficulty ?: "Medium",
                    calories = recipe.calories ?: "512 cal"
                )
                
                // Description
                RecipeDescription(
                    description = recipe.description ?: 
                        "A delicious recipe that's sure to please everyone at the table. Created by ${recipe.author}."
                )
                
                // Ingredients
                IngredientsList(
                    ingredients = recipe.ingredients ?: generateDefaultIngredients()
                )
                
                // Watch Videos Button
                ActionButton(
                    text = "Watch Videos",
                    onClick = { /* TODO: Open video player */ }
                )
            }
        }
    }
}

// Fallback function to generate default ingredients if none are provided
private fun generateDefaultIngredients() = listOf(
    com.example.myapplication.models.Ingredient("Ingredient 1", "100g"),
    com.example.myapplication.models.Ingredient("Ingredient 2", "2 tbsp"),
    com.example.myapplication.models.Ingredient("Ingredient 3", "1 cup")
) 
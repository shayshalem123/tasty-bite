package com.example.myapplication.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Recipe
import com.example.myapplication.ui.screens.detail.components.*
import com.example.myapplication.auth.UserViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBackClick: () -> Unit,
    userViewModel: UserViewModel
) {
    // Get creator display name from UserViewModel
    val creatorDisplayName by userViewModel.getUserDisplayName(recipe.createdBy).collectAsState()
    
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
                imageUrl = recipe.imageUrl,
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
                RecipeHeader(recipe = recipe, userViewModel = userViewModel)
                
                // Description
                RecipeDescription(
                    description = recipe.description ?: 
                        "A delicious recipe that's sure to please everyone at the table. Created by $creatorDisplayName."
                )
                
                // Ingredients
                IngredientsList(
                    ingredients = recipe.ingredients ?: generateDefaultIngredients()
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
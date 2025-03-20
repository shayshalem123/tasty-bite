package com.example.myapplication.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Recipe
import com.example.myapplication.ui.components.RecipeCard
import com.example.myapplication.ui.components.SectionHeader

@Composable
fun RecommendationsSection(
    title: String = "Recipes",
    recipes: List<Recipe> = emptyList(),
    onRecipeClick: (Recipe) -> Unit = {}
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        SectionHeader(title = title)
        
        if (recipes.isEmpty()) {
            // Show empty state message
            EmptyRecipesMessage()
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(recipes) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRecipesMessage() {
    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No recipes found",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
} 
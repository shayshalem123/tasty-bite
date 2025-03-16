package com.example.myapplication.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.recommendedRecipes
import com.example.myapplication.ui.components.RecipeCard
import com.example.myapplication.ui.components.SectionHeader

@Composable
fun RecommendationsSection() {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        SectionHeader(title = "Recommendation")
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            items(recommendedRecipes) { recipe ->
                RecipeCard(recipe)
            }
        }
    }
} 
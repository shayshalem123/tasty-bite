package com.example.myapplication.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.recommendedRecipes
import com.example.myapplication.models.Recipe
import com.example.myapplication.ui.components.BottomNavigationBar
import com.example.myapplication.ui.screens.detail.RecipeDetailScreen
import com.example.myapplication.ui.screens.home.HomeScreen

@Composable
fun TastyBiteApp() {
    // State to track the selected recipe
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedRecipe != null) {
            // Show recipe detail screen
            RecipeDetailScreen(
                recipe = selectedRecipe!!,
                onBackClick = { selectedRecipe = null }
            )
        } else {
            // Show home screen with bottom navigation and FAB
            Scaffold(
                bottomBar = { BottomNavigationBar() },
                floatingActionButton = { },
                floatingActionButtonPosition = FabPosition.Center
            ) { padding ->
                HomeScreen(
                    modifier = Modifier.padding(padding),
                    onRecipeClick = { recipe -> selectedRecipe = recipe }
                )
            }
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-28).dp)
            ) {
                FloatingActionButton(
                    onClick = { /* TODO: Add new recipe */ },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Recipe",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
} 
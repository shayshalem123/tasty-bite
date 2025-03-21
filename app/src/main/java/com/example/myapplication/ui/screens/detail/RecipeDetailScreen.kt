package com.example.myapplication.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Recipe
import com.example.myapplication.ui.screens.detail.components.*
import com.example.myapplication.auth.UserViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication.auth.AuthViewModel
import com.example.myapplication.data.RecipesDataRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBackClick: () -> Unit,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    onEditRecipe: (Recipe) -> Unit,
    onRecipeDeleted: () -> Unit
) {
    // Get creator display name from UserViewModel
    val creatorDisplayName by userViewModel.getUserDisplayName(recipe.createdBy).collectAsState()
    
    // Get current user to check if this recipe belongs to them
    val currentUser by authViewModel.currentUser.collectAsState()
    val isCreator = currentUser?.email == recipe.createdBy
    
    // Menu state
    var showMenu by remember { mutableStateOf(false) }
    
    // Delete confirmation dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Repository for recipe operations
    val recipesRepository = remember { RecipesDataRepository() }
    
    // Coroutine scope for async operations
    val coroutineScope = rememberCoroutineScope()
    
    // Loading and error states
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recipe") },
            text = { Text("Are you sure you want to delete this recipe? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        isLoading = true
                        
                        coroutineScope.launch {
                            recipesRepository.deleteRecipe(recipe.id)
                                .onSuccess {
                                    isLoading = false
                                    onRecipeDeleted()
                                }
                                .onFailure { error ->
                                    isLoading = false
                                    errorMessage = "Failed to delete recipe: ${error.message}"
                                }
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Error dialog
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                Button(
                    onClick = { errorMessage = null }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Loading dialog
    if (isLoading) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Please wait") },
            text = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("Processing...")
                }
            },
            confirmButton = { }
        )
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Recipe Image with navigation and menu
            Box {
                RecipeImageHeaderWithMenu(
                    imageUrl = recipe.imageUrl,
                    title = recipe.title,
                    onBackClick = onBackClick,
                    isCreator = isCreator,
                    onMoreClick = { showMenu = true }
                )
                
                // Show dropdown menu for edit/delete options
                if (isCreator) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Recipe") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Recipe"
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onEditRecipe(recipe)
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("Delete Recipe") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Recipe"
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
            
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
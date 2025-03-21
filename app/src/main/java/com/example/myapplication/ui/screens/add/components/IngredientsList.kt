package com.example.myapplication.ui.screens.add.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.models.Ingredient
import com.example.myapplication.data.api.SpoonacularApiService
import com.example.myapplication.ui.ingredients.IngredientInputComponent
import com.example.myapplication.ui.ingredients.IngredientSearchViewModel

@Composable
fun IngredientsList(
    ingredients: List<Ingredient>,
    onIngredientsChanged: (List<Ingredient>) -> Unit,
) {
    // Create ViewModel for ingredient search
    val viewModel = remember { IngredientSearchViewModel(SpoonacularApiService()) }
    
    // Local state for ingredient form
    var ingredientAmount by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("g") } // Default unit
    var resetSearchField by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // List of current ingredients
        if (ingredients.isNotEmpty()) {
            // Fixed height LazyColumn to ensure visibility
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 250.dp)
                    .padding(bottom = 8.dp)
            ) {
                itemsIndexed(ingredients) { index, ingredient ->
                    IngredientItem(
                        ingredient = ingredient,
                        onDelete = {
                            val newList = ingredients.toMutableList()
                            newList.removeAt(index)
                            onIngredientsChanged(newList)
                        }
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }
        
        // Form to add new ingredient using Spoonacular API
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Ingredient search with API autocomplete
            IngredientInputComponent(
                viewModel = viewModel,
                onIngredientSelected = { ingredientResult ->
                    // Reset amount when new ingredient is selected
                    ingredientAmount = ""
                },
                resetTrigger = resetSearchField
            )
            
            // Amount and unit selection
            IngredientAmountField(
                amount = ingredientAmount,
                onAmountChange = { ingredientAmount = it },
                selectedUnit = selectedUnit,
                onUnitChange = { selectedUnit = it },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Add button
            Button(
                onClick = {
                    val selectedIngredient = viewModel.suggestions.value.firstOrNull()
                    
                    if (selectedIngredient != null && ingredientAmount.isNotBlank()) {
                        val formattedAmount = "$ingredientAmount $selectedUnit"
                        val newIngredient = Ingredient(
                            name = selectedIngredient.name,
                            amount = formattedAmount,
                            imageUrl = R.drawable.placeholder_image
                            // You could use the Spoonacular image URL here if desired:
                            // imageUrl = "https://spoonacular.com/cdn/ingredients_100x100/${selectedIngredient.image}"
                        )
                        onIngredientsChanged(ingredients + newIngredient)
                        
                        // Reset form
                        viewModel.clearSuggestions()
                        ingredientAmount = ""
                        resetSearchField = !resetSearchField  // Toggle to trigger reset
                    }
                },
                enabled = viewModel.suggestions.value.isNotEmpty() && ingredientAmount.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Ingredient")
            }
        }
        
        // Show count of ingredients if any
        if (ingredients.isNotEmpty()) {
            Text(
                text = "${ingredients.size} ingredient(s) added",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun IngredientItem(
    ingredient: Ingredient,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = ingredient.amount,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Ingredient",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 
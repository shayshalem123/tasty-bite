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

@Composable
fun IngredientsList(
    ingredients: List<Ingredient>,
    onIngredientsChanged: (List<Ingredient>) -> Unit
) {
    var ingredientName by remember { mutableStateOf("") }
    var ingredientAmount by remember { mutableStateOf("") }
    
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
                    // Set a minimum height to ensure ingredients are visible
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
        
        // Form to add new ingredient
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = ingredientName,
                onValueChange = { ingredientName = it },
                label = { Text("Ingredient") },
                modifier = Modifier.weight(2f)
            )
            
            OutlinedTextField(
                value = ingredientAmount,
                onValueChange = { ingredientAmount = it },
                label = { Text("Amount") },
                modifier = Modifier.weight(1f)
            )
            
            Button(
                onClick = {
                    if (ingredientName.isNotBlank() && ingredientAmount.isNotBlank()) {
                        val newIngredient = Ingredient(
                            name = ingredientName,
                            amount = ingredientAmount,
                            imageUrl = R.drawable.placeholder_image
                        )
                        onIngredientsChanged(ingredients + newIngredient)
                        ingredientName = ""
                        ingredientAmount = ""
                    }
                },
                enabled = ingredientName.isNotBlank() && ingredientAmount.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
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
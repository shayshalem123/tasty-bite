package com.example.myapplication.ui.ingredients

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import com.example.myapplication.data.model.IngredientResult

@Composable
fun IngredientInputComponent(
    viewModel: IngredientSearchViewModel,
    onIngredientSelected: (IngredientResult) -> Unit,
    resetTrigger: Boolean = false
) {
    var query by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }
    
    LaunchedEffect(resetTrigger) {
        if (resetTrigger) {
            query = ""
            showSuggestions = false
        }
    }
    
    val suggestions by viewModel.suggestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Box {
        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                query = newQuery
                viewModel.searchIngredients(newQuery)
                showSuggestions = newQuery.length >= 2
            },
            label = { Text("Search Ingredient") },
            placeholder = { Text("Type ingredient name...") },
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f),
            singleLine = true
        )
        
        if (showSuggestions && (suggestions.isNotEmpty() || isLoading)) {
            Popup(
                alignment = Alignment.TopCenter,
                onDismissRequest = { showSuggestions = false }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = 60.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(8.dp)
                            )
                        } else {
                            suggestions.forEach { ingredient ->
                                Text(
                                    text = ingredient.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            query = ingredient.name
                                            showSuggestions = false
                                            onIngredientSelected(ingredient)
                                        }
                                        .padding(12.dp)
                                )
                                if (ingredient != suggestions.last()) {
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color.LightGray)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 
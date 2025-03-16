package com.example.myapplication.ui.screens.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.data.categories
import com.example.myapplication.models.Ingredient
import com.example.myapplication.models.Recipe
import com.example.myapplication.ui.screens.add.components.CategorySelector
import com.example.myapplication.ui.screens.add.components.FormTextField
import com.example.myapplication.ui.screens.add.components.IngredientsList
import com.example.myapplication.ui.screens.add.components.NumericFormField
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    onBackClick: () -> Unit,
    onRecipeAdded: (Recipe) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cookingTime by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var ingredients by remember { mutableStateOf<List<Ingredient>>(emptyList()) }
    
    // Validation state
    var showErrors by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf(false) }
    
    // Form is valid when these required fields are filled
    val isFormValid = title.isNotBlank() && author.isNotBlank() && ingredients.isNotEmpty()

    // Remember the snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Create a coroutine scope
    val scope = rememberCoroutineScope()
    
    // Effect to show snackbar when showErrorSnackbar is true
    LaunchedEffect(showErrorSnackbar) {
        if (showErrorSnackbar) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Please fill in all required fields",
                    actionLabel = "OK"
                )
            }
            showErrorSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (isFormValid) {
                                // Format values to include units
                                val formattedTime = if (cookingTime.isNotBlank()) "${cookingTime} mins" else ""
                                val formattedCalories = if (calories.isNotBlank()) "${calories} cal" else ""
                                
                                // Create and save the new recipe
                                val newRecipe = Recipe(
                                    id = UUID.randomUUID().toString(),
                                    title = title,
                                    author = author,
                                    imageUrl = R.drawable.placeholder_image,
                                    categories = selectedCategories,
                                    description = description,
                                    cookingTime = formattedTime,
                                    difficulty = difficulty,
                                    calories = formattedCalories,
                                    ingredients = ingredients
                                )
                                onRecipeAdded(newRecipe)
                                onBackClick()
                            } else {
                                // Show validation errors
                                showErrors = true
                                showErrorSnackbar = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFormValid) MaterialTheme.colorScheme.primary 
                                             else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isFormValid) MaterialTheme.colorScheme.onPrimary 
                                          else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Save",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Show validation errors message if needed
            if (showErrors && !isFormValid) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buildString {
                                append("Please fill in the following required fields:")
                                if (title.isBlank()) append("\n• Recipe title")
                                if (author.isBlank()) append("\n• Author name")
                                if (ingredients.isEmpty()) append("\n• At least one ingredient")
                            },
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Basic Recipe Information
            FormTextField(
                value = title,
                onValueChange = { title = it },
                label = "Recipe Title *",
                placeholder = "Enter recipe name",
                isError = showErrors && title.isBlank()
            )
            
            FormTextField(
                value = author,
                onValueChange = { author = it },
                label = "Author *",
                placeholder = "Your name",
                isError = showErrors && author.isBlank()
            )
            
            FormTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                placeholder = "Describe your recipe",
                singleLine = false,
                maxLines = 5
            )
            
            // Recipe Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NumericFormField(
                    value = cookingTime,
                    onValueChange = { cookingTime = it },
                    label = "Cooking Time",
                    placeholder = "30",
                    suffix = "mins",
                    modifier = Modifier.weight(1f)
                )
                
                NumericFormField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = "Calories",
                    placeholder = "450",
                    suffix = "cal",
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Difficulty Dropdown
            var expanded by remember { mutableStateOf(false) }
            val difficultyOptions = listOf("Easy", "Medium", "Hard")
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = difficulty,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Difficulty") },
                    placeholder = { Text("Select difficulty") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    difficultyOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                difficulty = option
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Category Selection
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium
            )
            
            CategorySelector(
                categories = categories,
                selectedCategoryIds = selectedCategories,
                onCategorySelected = { categoryId ->
                    selectedCategories = if (selectedCategories.contains(categoryId)) {
                        selectedCategories - categoryId
                    } else {
                        selectedCategories + categoryId
                    }
                }
            )
            
            // Ingredients Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "Ingredients *",
                    style = MaterialTheme.typography.titleMedium
                )
                if (showErrors && ingredients.isEmpty()) {
                    Text(
                        text = " (Required)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            IngredientsList(
                ingredients = ingredients,
                onIngredientsChanged = { ingredients = it }
            )
            
            // Required fields note
            Text(
                text = "* Required fields",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }
    }
} 
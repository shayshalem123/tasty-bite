package com.example.myapplication.ui.screens.add

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.auth.AuthViewModel
import com.example.myapplication.models.Ingredient
import com.example.myapplication.models.Recipe
import com.example.myapplication.ui.screens.add.components.IngredientAmountField
import kotlinx.coroutines.launch
import java.util.*
import androidx.lifecycle.ViewModel
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    recipe: Recipe,
    onBackClick: () -> Unit,
    onRecipeUpdated: (Recipe) -> Unit,
    authViewModel: AuthViewModel,
    viewModel: EditRecipeViewModel = EditRecipeViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Set the recipe to be edited in the ViewModel
    LaunchedEffect(recipe) {
        viewModel.setRecipe(recipe)
    }

    // Form state
    var title by remember { mutableStateOf(recipe.title) }
    var description by remember { mutableStateOf(recipe.description ?: "") }
    var category by remember { mutableStateOf(recipe.category) }
    var servings by remember { mutableStateOf(recipe.servings.toString()) }
    var cookTime by remember { mutableStateOf(recipe.cookTime.toString()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val ingredients = remember { mutableStateListOf<Ingredient>() }
    val instructions = remember { mutableStateListOf<String>() }

    // State for loading and errors
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Form validation state
    var showErrors by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var servingsError by remember { mutableStateOf<String?>(null) }
    var cookTimeError by remember { mutableStateOf<String?>(null) }
    var instructionsError by remember { mutableStateOf<String?>(null) }
    var ingredientsError by remember { mutableStateOf<String?>(null) }

    // Initialize lists
    LaunchedEffect(recipe) {
        // Clear existing data
        ingredients.clear()
        instructions.clear()

        // Add recipe ingredients
        recipe.ingredients?.let { ingredients.addAll(it) }

        // Add recipe instructions
        instructions.addAll(recipe.instructions)
    }

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Validate image
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)

            // Check file type
            val isValidType = mimeType == "image/jpeg" || mimeType == "image/png"

            // Check file size (limit to 2MB)
            val fileSize = try {
                contentResolver.openInputStream(uri)?.use { it.available() } ?: 0
            } catch (e: Exception) {
                -1
            }
            val isValidSize = fileSize in 1..2 * 1024 * 1024 // 2MB max

            when {
                !isValidType -> {
                    errorMessage = "Invalid format. Please select a JPEG or PNG image."
                }

                !isValidSize -> {
                    errorMessage = "Image is too large. Maximum size is 2MB."
                }

                else -> {
                    selectedImageUri = it
                }
            }
        }
    }

    // SaveState observer
    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Saving -> {
                isLoading = true
                errorMessage = null
            }

            is SaveState.Success -> {
                isLoading = false
                errorMessage = null
                onRecipeUpdated(recipe)
            }

            is SaveState.Error -> {
                isLoading = false
                errorMessage = (saveState as SaveState.Error).message
            }

            else -> {
                isLoading = false
            }
        }
    }

    // Reset save state when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetSaveState()
        }
    }

    // Error dialog
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "An unexpected error occurred") },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    // Loading dialog
    if (isLoading) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Updating Recipe") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("Please wait...")
                }
            },
            confirmButton = { }
        )
    }

    // Validate function
    fun validateForm(): Boolean {
        showErrors = true

        // Validate title
        titleError = when {
            title.isBlank() -> "Title is required"
            title.length < 3 -> "Title is too short"
            else -> null
        }

        // Validate servings
        servingsError = when {
            servings.isBlank() -> "Servings is required"
            servings.toIntOrNull() == null -> "Must be a number"
            servings.toInt() <= 0 -> "Must be greater than 0"
            else -> null
        }

        // Validate cook time
        cookTimeError = when {
            cookTime.isBlank() -> "Cook time is required"
            cookTime.toIntOrNull() == null -> "Must be a number"
            cookTime.toInt() <= 0 -> "Must be greater than 0"
            else -> null
        }

        // Validate instructions
        instructionsError = when {
            instructions.isEmpty() -> "At least one instruction is required"
            instructions.any { it.isBlank() } -> "Instructions cannot be empty"
            else -> null
        }

        // Validate ingredients
        ingredientsError = when {
            ingredients.isEmpty() -> "At least one ingredient is required"
            ingredients.any { it.name.isBlank() || it.amount.isBlank() } -> "Ingredients must have name and amount"
            else -> null
        }

        return titleError == null && servingsError == null && cookTimeError == null
                && instructionsError == null && ingredientsError == null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (validateForm()) {
                                val currentUser = authViewModel.currentUser.value

                                if (currentUser != null) {
                                    val updatedRecipe = Recipe(
                                        id = recipe.id,
                                        title = title,
                                        imageUrl = recipe.imageUrl, // This will be updated by the viewModel if a new image is selected
                                        description = description.takeIf { it.isNotBlank() },
                                        ingredients = ingredients.toList(),
                                        instructions = instructions.toList(),
                                        cookTime = cookTime.toIntOrNull() ?: 0,
                                        servings = servings.toIntOrNull() ?: 0,
                                        category = category,
                                        createdBy = recipe.createdBy,
                                        createdAt = recipe.createdAt,
                                        isFavorite = recipe.isFavorite
                                    )

                                    viewModel.updateRecipe(
                                        recipe = updatedRecipe,
                                        imageUri = selectedImageUri,
                                        onSuccess = { updatedRecipe ->
                                            onRecipeUpdated(updatedRecipe)
                                        }
                                    )
                                } else {
                                    errorMessage = "You must be logged in to update a recipe"
                                }
                            } else {
                                errorMessage = "Please correct the errors in the form"
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Recipe image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    // If there's a selected image or existing image URL, show it
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Recipe Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (recipe.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = recipe.imageUrl,
                            contentDescription = "Recipe Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Image",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to select an image")
                        }
                    }

                    // Show change overlay
                    if (selectedImageUri != null || recipe.imageUrl.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                .clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Change Image",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                // Title
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Recipe Title") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showErrors && titleError != null,
                    supportingText = {
                        if (showErrors && titleError != null) {
                            Text(titleError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }

            item {
                // Description
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )
            }

            item {
                // Category
                TextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                // Cook time and servings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextField(
                        value = cookTime,
                        onValueChange = { cookTime = it },
                        label = { Text("Cook Time (minutes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        isError = showErrors && cookTimeError != null,
                        supportingText = {
                            if (showErrors && cookTimeError != null) {
                                Text(cookTimeError!!, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )

                    TextField(
                        value = servings,
                        onValueChange = { servings = it },
                        label = { Text("Servings") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        isError = showErrors && servingsError != null,
                        supportingText = {
                            if (showErrors && servingsError != null) {
                                Text(servingsError!!, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            }

            // Ingredients
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ingredients",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    if (showErrors && ingredientsError != null) {
                        Text(
                            text = ingredientsError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Ingredient items
            itemsIndexed(ingredients) { index, ingredient ->
                EnhancedIngredientItem(
                    ingredient = ingredient,
                    onDelete = { ingredients.removeAt(index) },
                    onUpdate = { updatedIngredient ->
                        ingredients[index] = updatedIngredient
                    }
                )
            }

            // Add ingredient button
            item {
                AddIngredientSection(
                    onAddIngredient = { newIngredient ->
                        ingredients.add(newIngredient)
                    }
                )
            }

            // Instructions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    if (showErrors && instructionsError != null) {
                        Text(
                            text = instructionsError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Instruction items
            itemsIndexed(instructions) { index, instruction ->
                InstructionItem(
                    index = index + 1,
                    instruction = instruction,
                    onDelete = { instructions.removeAt(index) },
                    onUpdate = { updatedInstruction ->
                        instructions[index] = updatedInstruction
                    }
                )
            }

            // Add instruction button
            item {
                OutlinedButton(
                    onClick = {
                        instructions.add("")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Instruction")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Instruction")
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedIngredientItem(
    ingredient: Ingredient,
    onDelete: () -> Unit,
    onUpdate: (Ingredient) -> Unit
) {
    var name by remember { mutableStateOf(ingredient.name) }
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("g") }

    // Parse the amount and unit from the ingredient amount string
    LaunchedEffect(ingredient) {
        val parts = ingredient.amount.split(" ")
        if (parts.isNotEmpty()) {
            amount = parts[0]
            if (parts.size > 1) {
                unit = parts[1]
            }
        }
    }

    // Update the ingredient whenever name, amount, or unit changes
    LaunchedEffect(name, amount, unit) {
        if (amount.isNotBlank()) {
            onUpdate(Ingredient(name, "$amount $unit", ingredient.imageUrl))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Ingredient") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IngredientAmountField(
                    amount = amount,
                    onAmountChange = { amount = it },
                    selectedUnit = unit,
                    onUnitChange = { unit = it },
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddIngredientSection(
    onAddIngredient: (Ingredient) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("g") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Add New Ingredient",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Ingredient Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            IngredientAmountField(
                amount = amount,
                onAmountChange = { amount = it },
                selectedUnit = unit,
                onUnitChange = { unit = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && amount.isNotBlank()) {
                        onAddIngredient(Ingredient(name, "$amount $unit"))
                        name = ""
                        amount = ""
                        // Keep the unit for convenience
                    }
                },
                enabled = name.isNotBlank() && amount.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstructionItem(
    index: Int,
    instruction: String,
    onDelete: () -> Unit,
    onUpdate: (String) -> Unit
) {
    var text by remember { mutableStateOf(instruction) }

    // Update the instruction whenever text changes
    LaunchedEffect(text) {
        onUpdate(text)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "$index.",
                modifier = Modifier
                    .padding(top = 20.dp, end = 8.dp)
                    .width(24.dp)
            )

            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Step $index") },
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
} 
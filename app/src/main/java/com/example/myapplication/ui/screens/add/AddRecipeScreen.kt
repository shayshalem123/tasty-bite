package com.example.myapplication.ui.screens.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.myapplication.auth.AuthViewModel
import com.example.myapplication.data.categories
import com.example.myapplication.models.Ingredient
import com.example.myapplication.models.Recipe
import com.example.myapplication.ui.screens.add.components.CategorySelector
import com.example.myapplication.ui.screens.add.components.FormTextField
import com.example.myapplication.ui.screens.add.components.IngredientsList
import com.example.myapplication.ui.screens.add.components.NumericFormField
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    onBackClick: () -> Unit,
    onRecipeAdded: (Recipe) -> Unit,
    onRecipeUpdated: (Recipe) -> Unit = {},
    recipeToEdit: Recipe? = null,
    authViewModel: AuthViewModel,
    addRecipeViewModel: AddRecipeViewModel = remember { AddRecipeViewModel() }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Set the recipe to edit in the ViewModel
    LaunchedEffect(recipeToEdit) {
        addRecipeViewModel.setRecipeToEdit(recipeToEdit)
    }
    
    val isEditMode by addRecipeViewModel.isEditMode.collectAsState()
    val editingRecipe by addRecipeViewModel.recipe.collectAsState()
    
    // Form state with initial values from recipe if in edit mode
    var title by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.title ?: "") }
    var description by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.description ?: "") }
    var category by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.category ?: "") }
    var servings by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.servings?.toString() ?: "") }
    var cookTime by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.cookTime?.toString() ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val ingredients = remember(recipeToEdit) {
        mutableStateListOf<Ingredient>().apply {
            recipeToEdit?.ingredients?.let { addAll(it) }
        }
    }
    val instructions = remember(recipeToEdit) {
        mutableStateListOf<String>().apply {
            recipeToEdit?.instructions?.let { addAll(it) }
        }
    }

    // Additional form fields with values from recipe if in edit mode
    var cookingTime by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.cookingTime?.replace(" mins", "") ?: "") }
    var difficulty by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.difficulty ?: "") }
    var calories by remember(recipeToEdit) { mutableStateOf(recipeToEdit?.calories?.replace(" cal", "") ?: "") }
    var selectedCategories by remember(recipeToEdit) { 
        // Safely handle nullable categories from the recipe being edited
        mutableStateOf(recipeToEdit?.categories?.filterNotNull() ?: emptyList()) 
    }
    var showErrors by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf(false) }

    val isFormValid = title.isNotBlank() && ingredients.isNotEmpty() && 
        (selectedImageUri != null || (isEditMode && recipeToEdit?.imageUrl?.isNotEmpty() == true))

    val snackbarHostState = remember { SnackbarHostState() }

    // Image selection state
    var imageErrorMessage by remember { mutableStateOf<String?>(null) }

    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Validate the image when selected
        if (uri != null) {
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
                    imageErrorMessage = "Invalid format. Please select a JPEG or PNG image."
                    selectedImageUri = null
                }

                !isValidSize -> {
                    imageErrorMessage = "Image is too large. Maximum size is 2MB."
                    selectedImageUri = null
                }

                else -> {
                    selectedImageUri = uri
                    imageErrorMessage = null
                }
            }
        }
    }

    // Collect upload state and progress
    val uploadState by addRecipeViewModel.uploadState.collectAsState()

    // Effect to show image error message when upload fails
    LaunchedEffect(uploadState) {
        if (uploadState is UploadState.Error) {
            imageErrorMessage = (uploadState as UploadState.Error).message
        }
    }

    // SaveState observer
    val saveState by addRecipeViewModel.saveState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Saving -> {
                isLoading = true
                errorMessage = null
            }
            is SaveState.Success -> {
                isLoading = false
                errorMessage = null
                // Different callbacks for add vs edit
                if (isEditMode) {
                    editingRecipe?.let { onRecipeUpdated(it) }
                } else {
                    // This will be called after adding a new recipe
                    addRecipeViewModel.recipe.value?.let { onRecipeAdded(it) }
                }
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

    // Debug logging to diagnose category selection issues
    LaunchedEffect(recipeToEdit) {
        Log.d("AddRecipeScreen", "Recipe categories: ${recipeToEdit?.categories}")
        Log.d("AddRecipeScreen", "Selected categories: $selectedCategories")
    }

    if (isLoading) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Add a retry button if error
    if (saveState is SaveState.Error) {
        Button(
            onClick = {
                if (validateForm(title, instructions)) {
                    val currentUser = authViewModel.currentUser.value
                    
                    if (currentUser != null) {
                        val recipeData = Recipe(
                            id = recipeToEdit?.id ?: "",
                            title = title,
                            imageUrl = recipeToEdit?.imageUrl ?: "",
                            description = description.takeIf { it.isNotBlank() },
                            ingredients = ingredients.toList(),
                            instructions = instructions.toList(),
                            cookTime = cookTime.toIntOrNull() ?: 0,
                            servings = servings.toIntOrNull() ?: 0,
                            category = category.ifBlank { 
                                selectedCategories.firstOrNull() ?: "" 
                            },
                            cookingTime = if (cookingTime.isNotBlank()) "${cookingTime} mins" else null,
                            difficulty = difficulty.takeIf { it.isNotBlank() },
                            calories = if (calories.isNotBlank()) "${calories} cal" else null,
                            categories = selectedCategories.toList(),
                            createdBy = recipeToEdit?.createdBy ?: currentUser.email,
                            createdAt = recipeToEdit?.createdAt ?: System.currentTimeMillis(),
                            isFavorite = recipeToEdit?.isFavorite ?: false
                        )
                        
                        // Debug logging for the recipe's categories
                        Log.d("AddRecipeScreen", "Saving recipe with categories: ${recipeData.categories}")
                        
                        if (isEditMode) {
                            addRecipeViewModel.updateRecipe(
                                recipe = recipeData,
                                imageUri = selectedImageUri,
                                onSuccess = { updatedRecipe ->
                                    onRecipeUpdated(updatedRecipe)
                                }
                            )
                        } else {
                            addRecipeViewModel.saveRecipe(
                                recipe = recipeData,
                                imageUri = selectedImageUri,
                                onSuccess = { savedRecipe ->
                                    onRecipeAdded(savedRecipe)
                                }
                            )
                        }
                    } else {
                        errorMessage = "You must be logged in to ${if (isEditMode) "update" else "add"} a recipe"
                    }
                } else {
                    errorMessage = "Please fill in all required fields (title and at least one instruction)"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Retry")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Recipe" else "Add Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (validateForm(title, instructions)) {
                                val currentUser = authViewModel.currentUser.value
                                
                                if (currentUser != null) {
                                    val recipeData = Recipe(
                                        id = recipeToEdit?.id ?: "",
                                        title = title,
                                        imageUrl = recipeToEdit?.imageUrl ?: "",
                                        description = description.takeIf { it.isNotBlank() },
                                        ingredients = ingredients.toList(),
                                        instructions = instructions.toList(),
                                        cookTime = cookTime.toIntOrNull() ?: 0,
                                        servings = servings.toIntOrNull() ?: 0,
                                        category = category.ifBlank { 
                                            selectedCategories.firstOrNull() ?: "" 
                                        },
                                        cookingTime = if (cookingTime.isNotBlank()) "${cookingTime} mins" else null,
                                        difficulty = difficulty.takeIf { it.isNotBlank() },
                                        calories = if (calories.isNotBlank()) "${calories} cal" else null,
                                        categories = selectedCategories.toList(),
                                        createdBy = recipeToEdit?.createdBy ?: currentUser.email,
                                        createdAt = recipeToEdit?.createdAt ?: System.currentTimeMillis(),
                                        isFavorite = recipeToEdit?.isFavorite ?: false
                                    )
                                    
                                    // Debug logging for the recipe's categories
                                    Log.d("AddRecipeScreen", "Saving recipe with categories: ${recipeData.categories}")
                                    
                                    if (isEditMode) {
                                        addRecipeViewModel.updateRecipe(
                                            recipe = recipeData,
                                            imageUri = selectedImageUri,
                                            onSuccess = { updatedRecipe ->
                                                onRecipeUpdated(updatedRecipe)
                                            }
                                        )
                                    } else {
                                        addRecipeViewModel.saveRecipe(
                                            recipe = recipeData,
                                            imageUri = selectedImageUri,
                                            onSuccess = { savedRecipe ->
                                                onRecipeAdded(savedRecipe)
                                            }
                                        )
                                    }
                                } else {
                                    errorMessage = "You must be logged in to ${if (isEditMode) "update" else "add"} a recipe"
                                }
                            } else {
                                errorMessage = "Please fill in all required fields (title and at least one instruction)"
                            }
                        }
                    ) {
                        Text(if (isEditMode) "Save" else "Add")
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
                                if (ingredients.isEmpty()) append("\n• At least one ingredient")
                                if (selectedImageUri == null) append("\n• Recipe image")
                            },
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Basic Recipe Information
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Recipe Title") },
                modifier = Modifier.fillMaxWidth(),
                isError = showErrors && title.isBlank()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth()
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
                text = "Categories (Select one or more)",
                style = MaterialTheme.typography.titleMedium
            )

            CategorySelector(
                categories = categories,
                selectedCategoryIds = selectedCategories,
                onCategorySelected = { categoryId ->
                    Log.d("AddRecipeScreen", "Category $categoryId selected/deselected")
                    Log.d("AddRecipeScreen", "Before change: $selectedCategories")
                    
                    selectedCategories = if (selectedCategories.contains(categoryId)) {
                        selectedCategories.filter { it != categoryId }
                    } else {
                        selectedCategories + categoryId
                    }
                    
                    Log.d("AddRecipeScreen", "After change: $selectedCategories")
                }
            )
            
            // Show selected categories summary
            if (selectedCategories.isNotEmpty()) {
                val selectedCategoryNames = selectedCategories.mapNotNull { categoryId ->
                    categories.find { it.id == categoryId }?.name
                }
                
                Text(
                    text = "Selected: ${selectedCategoryNames.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

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
                onIngredientsChanged = { newIngredients -> 
                    ingredients.clear()
                    ingredients.addAll(newIngredients)
                }
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

            // Add instructions field
            FormTextField(
                value = instructions.joinToString(separator = "\n"),
                onValueChange = { text ->
                    val newInstructions = text.split("\n").filter { it.isNotBlank() }
                    instructions.clear()
                    instructions.addAll(newInstructions)
                },
                label = "Instructions (one step per line)",
                placeholder = "Enter cooking instructions",
                singleLine = false,
                maxLines = 8
            )

            // Add servings field
            NumericFormField(
                value = servings,
                onValueChange = { servings = it },
                label = "Servings",
                placeholder = "4",
                suffix = "servings",
                modifier = Modifier.fillMaxWidth()
            )

            // Image selection component
            Column(
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recipe Image *",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (showErrors && selectedImageUri == null) {
                        Text(
                            text = " (Required)",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                        )
                    }
                }

                // Button to select image
                Button(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Select Image",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                    Text("Select Image")
                }

                // Show image error message if any
                imageErrorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Preview selected image
                if (selectedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(selectedImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Recipe image preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else if (isEditMode && recipeToEdit?.imageUrl?.isNotEmpty() == true) {
                    // Show existing image when editing
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(recipeToEdit?.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Recipe image preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Show error if upload failed
                if (uploadState is UploadState.Error) {
                    Text(
                        text = "Upload failed: ${(uploadState as UploadState.Error).message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Error message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    // Reset ViewModel state when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            addRecipeViewModel.resetAllState()
        }
    }
}

// Helper functions
private fun validateForm(title: String, instructions: List<String>): Boolean {
    return title.isNotBlank() && instructions.isNotEmpty() && !instructions.any { it.isBlank() }
}

// ... existing helper functions ... 
// ... existing helper functions ... 
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    onBackClick: () -> Unit,
    onRecipeAdded: (Recipe) -> Unit,
    authViewModel: AuthViewModel,
    addRecipeViewModel: AddRecipeViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val saveState by addRecipeViewModel.saveState.collectAsState()

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cookingTime by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var ingredients by remember { mutableStateOf<List<Ingredient>>(emptyList()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var servingsCount by remember { mutableStateOf("4") }
    var instructions by remember { mutableStateOf("") }

    var showErrors by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf(false) }

    val isFormValid =
        title.isNotBlank() && author.isNotBlank() && ingredients.isNotEmpty() && selectedImageUri != null

    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

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

    // Image selection state
    var imageErrorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

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

    // Handle form submission
    val onSubmit: Function0<Unit> = {
        if (isFormValid) {
            // Reset any previous errors
            addRecipeViewModel.resetSaveState()
            
            // Create the recipe object
            val newRecipe = Recipe(
                id = "",  // This will be set by the service
                title = title,
                author = author,
                imageUrl = "",  // This will be set by the service after upload
                categories = selectedCategories,
                description = description,
                cookingTime = if (cookingTime.isNotBlank()) "${cookingTime} mins" else "",
                difficulty = difficulty,
                calories = if (calories.isNotBlank()) "${calories} cal" else "",
                ingredients = ingredients,
                instructions = if (instructions.isNotBlank()) {
                    instructions.split("\n")
                } else {
                    listOf("No instructions provided")
                },
                cookTime = cookingTime.toIntOrNull() ?: 30,
                servings = servingsCount.toIntOrNull() ?: 4,
                category = if (selectedCategories.isNotEmpty()) selectedCategories.first() else "other",
                createdBy = currentUser?.email ?: "anonymous"
            )
            
            // Let the ViewModel handle the save operation, including image upload
            addRecipeViewModel.saveRecipe(
                recipe = newRecipe,
                imageUri = selectedImageUri!!,  // Non-null assertion because isFormValid ensures it's not null
                onSuccess = { savedRecipe ->
                    // Handle successful save
                    onRecipeAdded(savedRecipe)
                    onBackClick()
                }
            )
        } else {
            // Show validation errors
            showErrors = true
            showErrorSnackbar = true
            
            // Reset save state if it's in loading state
            if (saveState is SaveState.Saving) {
                addRecipeViewModel.resetSaveState()
            }
        }
    }

    val debug by addRecipeViewModel.debug.collectAsState()

    debug?.let { debugMessage ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Firebase Debug",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = debugMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )

            }
        }
    }

    if (saveState is SaveState.Saving) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Add a retry button if error
    if (saveState is SaveState.Error) {
        Button(
            onClick = onSubmit,
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
                title = { Text("Add New Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFormValid) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isFormValid) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        if (saveState is SaveState.Saving && isFormValid) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Save")
                        }
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
                                if (selectedImageUri == null) append("\n• Recipe image")
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

            // Add instructions field
            FormTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = "Instructions (one step per line)",
                placeholder = "Enter cooking instructions",
                singleLine = false,
                maxLines = 8
            )

            // Add servings field
            NumericFormField(
                value = servingsCount,
                onValueChange = { servingsCount = it },
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
            if (saveState is SaveState.Error) {
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
                            text = "Error saving recipe",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = (saveState as SaveState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Remove the redundant LaunchedEffect since we're already handling navigation in the callback
            if (saveState is SaveState.Success) {
                // We now handle navigation in the saveRecipe callback above
                // This ensures we navigate after the recipe has been properly added
            }
        }
    }
} 
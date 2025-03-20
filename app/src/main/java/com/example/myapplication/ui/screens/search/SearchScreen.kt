package com.example.myapplication.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.categories
import com.example.myapplication.models.Recipe
import com.example.myapplication.models.Ingredient
import com.example.myapplication.ui.theme.TastyBiteGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    recipes: List<Recipe>,
    onBackClick: () -> Unit,
    onRecipeClick: (Recipe) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var showAllRecipes by remember { mutableStateOf(false) }

    // Filtered recipes based on search query
    val filteredRecipes = remember(searchQuery, recipes) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            recipes.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        (it.description?.contains(searchQuery, ignoreCase = true) ?: false) ||
                        (it.categories?.any { category ->
                            category.contains(
                                searchQuery,
                                ignoreCase = true
                            )
                        } ?: false) ||
                        (it.ingredients?.any { ingredient ->
                            ingredient.name.contains(
                                searchQuery,
                                ignoreCase = true
                            )
                        } ?: false)
            }
        }
    }

    // Popular search suggestions
    val popularSearches =
        listOf("Pasta", "Chicken", "Vegetarian", "Dessert", "Quick", "Healthy", "Breakfast")

    // Using system categories instead of hardcoded meal types
    var selectedCategoryId by remember { mutableStateOf("") }

    // Dietary filters
    val dietaryFilters = listOf("Vegetarian", "Vegan", "Gluten-Free", "Dairy-Free", "Low Carb")
    var selectedDietary by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            // Only show loading indicator if there are no existing results
            val shouldShowLoading = searchResults.isEmpty()
            
            if (shouldShowLoading) {
                isSearching = true
            }
            
            delay(300) // Debounce
            searchResults = filteredRecipes
            isSearching = false
            showAllRecipes = false
        } else {
            searchResults = emptyList()
        }
    }

    // Request focus when screen appears
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .focusRequester(focusRequester),
                placeholder = { Text("Search for recipes...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = TastyBiteGreen
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Food categories filter chips
            Text(
                text = "Categories",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // Add "All" option first
                item {
                    FilterChip(
                        selected = selectedCategoryId.isEmpty(),
                        onClick = { 
                            selectedCategoryId = ""
                            searchQuery = ""
                            showAllRecipes = true
                        },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TastyBiteGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                // Add all system categories
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { 
                            selectedCategoryId = category.id
                            coroutineScope.launch {
                                // Auto-fill search with the filter
                                searchQuery = category.name
                                showAllRecipes = false
                            }
                        },
                        label = { Text(category.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TastyBiteGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Only show popular searches and dietary restrictions when not in search mode
            if (searchQuery.isEmpty() && !showAllRecipes) {
                // Popular searches
                Text(
                    text = "Popular Searches",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(popularSearches) { search ->
                        SuggestionChip(
                            onClick = { 
                                searchQuery = search
                                showAllRecipes = false
                            },
                            label = { Text(search) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }

                // Dietary restrictions
                Text(
                    text = "Dietary Restrictions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(dietaryFilters) { filter ->
                        val isSelected = selectedDietary.contains(filter)
                        FilterChip(
                            selected = isSelected,
                            onClick = { 
                                selectedDietary = if (isSelected) {
                                    selectedDietary - filter
                                } else {
                                    selectedDietary + filter
                                }
                            },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TastyBiteGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search results or all recipes
            if (searchQuery.isNotEmpty()) {
                if (isSearching && searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TastyBiteGreen)
                    }
                } else if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recipes found for '$searchQuery'",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "Search Results (${searchResults.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(searchResults) { recipe ->
                            SearchResultItem(
                                recipe = recipe,
                                onClick = { onRecipeClick(recipe) }
                            )
                        }
                    }
                }
            } else if (showAllRecipes) {
                // Show all recipes when "All" is selected
                Text(
                    text = "All Recipes (${recipes.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recipes) { recipe ->
                        SearchResultItem(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(4.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            label()
        }
    }
}

@Composable
fun SearchResultItem(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 12.dp),
                tint = TastyBiteGreen
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recipe.ingredients?.take(2)?.joinToString(", ") { it.name } +
                            if (recipe.ingredients != null && recipe.ingredients.size > 2) "..." else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Display difficulty badge
            recipe.difficulty?.let { difficulty ->
                DifficultyBadge(difficulty = difficulty)
            }
        }
    }
}

@Composable
fun DifficultyBadge(difficulty: String) {
    val (backgroundColor, textColor) = when (difficulty.lowercase()) {
        "easy" -> TastyBiteGreen to Color.White
        "medium" -> Color(0xFFFF9800) to Color.White // Orange
        "hard" -> Color(0xFFF44336) to Color.White // Red
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(
            text = difficulty,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
} 
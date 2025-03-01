package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.categories
import com.example.myapplication.data.recommendedRecipes
import com.example.myapplication.models.Category
import com.example.myapplication.models.Recipe
import com.example.myapplication.ui.theme.TastyBiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TastyBiteTheme {
                TastyBiteApp()
            }
        }
    }
}

@Composable
fun TastyBiteApp() {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = { BottomNavigationBar() },
            floatingActionButton = { },
            floatingActionButtonPosition = FabPosition.Center
        ) { padding ->
            HomeScreen(Modifier.padding(padding))
        }
        
        // Custom positioned FAB
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

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        HomeHeader()
        
        // Search Bar
        SearchBar()
        
        // Categories
        CategoriesSection()
        
        // Recommendations
        RecommendationsSection()
        
        // Recipes of the Week
        RecipesOfTheWeekSection()
    }
}

@Composable
fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hello, Anne",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "What would you like\nto cook today?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Image(
            painter = painterResource(id = R.drawable.placeholder_image),
            contentDescription = "Profile",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        placeholder = { Text("Search any recipes") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = { Icon(painterResource(id = R.drawable.ic_filter), contentDescription = "Filter") },
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun CategoriesSection() {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        SectionHeader(title = "Categories")
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            items(categories) { category ->
                CategoryItem(category)
            }
        }
    }
}

@Composable
fun BottomNavigationBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            thickness = 1.dp
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconButton(
                                onClick = {},
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Home, 
                                    contentDescription = "Home",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            // Active indicator
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(2.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search, 
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Empty space for FAB
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .width(56.dp)
                    )
                    
                    IconButton(
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Saved",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person, 
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "See all",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp
        )
    }
}

@Composable
fun CategoryItem(category: Category) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Card(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Image(
                painter = painterResource(id = category.icon),
                contentDescription = category.name,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        Text(
            text = category.name,
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 12.sp
        )
    }
}

@Composable
fun RecommendationsSection() {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        SectionHeader(title = "Recommendation")
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            items(recommendedRecipes) { recipe ->
                RecipeCard(recipe)
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = recipe.imageUrl),
                contentDescription = recipe.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = recipe.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = recipe.author,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun RecipesOfTheWeekSection() {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        SectionHeader(title = "Recipes Of The Week")
        // Implementation similar to RecommendationsSection
    }
}
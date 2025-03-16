package com.example.myapplication.ui.screens.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.models.Recipe

@Composable
fun RecipeHeader(
    recipe: Recipe
) {
    // Extract values for cleaner code
    val title = recipe.title
    val author = recipe.author
    val rating = "4.5" // Hardcoded for now
    val cookingTime = recipe.cookingTime ?: "10 mins"
    val difficulty = recipe.difficulty ?: "Medium"
    val calories = recipe.calories ?: "512 cal"
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Title and Rating
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFC107),  // Amber
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = rating,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        
        // Author
        Text(
            text = "By $author",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        // Stats: Time, Difficulty, Calories
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecipeStat(
                iconRes = com.example.myapplication.R.drawable.placeholder_image, // Replace with clock icon
                label = cookingTime,
                modifier = Modifier.weight(1f)
            )
            
            RecipeStat(
                iconRes = com.example.myapplication.R.drawable.placeholder_image, // Replace with difficulty icon
                label = difficulty,
                modifier = Modifier.weight(1f)
            )
            
            RecipeStat(
                iconRes = com.example.myapplication.R.drawable.placeholder_image, // Replace with calorie icon
                label = calories,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RecipeStat(
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 14.sp
        )
    }
} 
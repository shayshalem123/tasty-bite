package com.example.myapplication.ui.screens.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.categories
import com.example.myapplication.models.Category
import com.example.myapplication.ui.components.CategoryItem
import com.example.myapplication.ui.components.SectionHeader
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import android.util.Log

@Composable
fun CategoriesSection(
    selectedCategory: Category? = null,
    onCategorySelected: (Category) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        SectionHeader(title = "Categories")
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    isSelected = selectedCategory?.id == category.id,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
        
        // Uncomment this for testing if needed
        /*
        if (selectedCategory != null) {
            Button(
                onClick = { 
                    Log.d("Categories", "Selected category: ${selectedCategory.id} - ${selectedCategory.name}")
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Debug Category")
            }
        }
        */
    }
} 
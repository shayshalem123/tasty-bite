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
    }
} 
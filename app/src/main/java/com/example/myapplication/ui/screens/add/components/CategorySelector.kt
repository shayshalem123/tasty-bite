package com.example.myapplication.ui.screens.add.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.models.Category
import com.example.myapplication.ui.components.CategoryItem

@Composable
fun CategorySelector(
    categories: List<Category>,
    selectedCategoryIds: List<String>,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(categories) { category ->
            CategoryItem(
                category = category,
                isSelected = selectedCategoryIds.contains(category.id),
                onClick = { onCategorySelected(category.id) }
            )
        }
    }
} 
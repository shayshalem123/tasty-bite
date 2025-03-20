package com.example.myapplication.data

import com.example.myapplication.R
import com.example.myapplication.models.Category

// Categories - keeping these as they're still needed for UI
val categories = listOf(
    Category("pasta", "Pasta", R.drawable.placeholder_image),
    Category("pizza", "Pizza", R.drawable.placeholder_image),
    Category("salad", "Salad", R.drawable.placeholder_image),
    Category("dessert", "Dessert", R.drawable.placeholder_image),
    Category("soup", "Soup", R.drawable.placeholder_image),
    Category("chicken", "Chicken", R.drawable.placeholder_image)
)

// Remove all sample recipes as they'll now come from Firestore 
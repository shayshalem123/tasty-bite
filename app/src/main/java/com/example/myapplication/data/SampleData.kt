package com.example.myapplication.data

import com.example.myapplication.R
import com.example.myapplication.models.Category

// Categories - keeping these as they're still needed for UI
val categories = listOf(
    Category("pasta", "Pasta", R.drawable.pasta),
    Category("pizza", "Pizza", R.drawable.pizza),
    Category("salad", "Salad", R.drawable.salad),
    Category("dessert", "Dessert", R.drawable.dessert),
    Category("soup", "Soup", R.drawable.soup),
    Category("chicken", "Chicken", R.drawable.chicken)
)

// Remove all sample recipes as they'll now come from Firestore 
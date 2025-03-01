package com.example.myapplication.data

import com.example.myapplication.R
import com.example.myapplication.models.Category
import com.example.myapplication.models.Recipe

val categories = listOf(
    Category("1", "Breakfast", R.drawable.placeholder_image),
    Category("2", "Lunch", R.drawable.placeholder_image),
    Category("3", "Dinner", R.drawable.placeholder_image),
    Category("4", "Dessert", R.drawable.placeholder_image)
)

val recommendedRecipes = listOf(
    Recipe("1", "Creamy Pasta", "By David Charles", R.drawable.placeholder_image),
    Recipe("2", "Macarons", "By Rachel William", R.drawable.placeholder_image),
    Recipe("3", "Chicken Stir Fry", "By Sam Smith", R.drawable.placeholder_image)
) 
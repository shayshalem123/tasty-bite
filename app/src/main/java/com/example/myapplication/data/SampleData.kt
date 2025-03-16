package com.example.myapplication.data

import com.example.myapplication.R
import com.example.myapplication.models.Category
import com.example.myapplication.models.Recipe

val categories = listOf(
    Category("pasta", "Pasta", R.drawable.placeholder_image),
    Category("chicken", "Chicken", R.drawable.placeholder_image),
    Category("pizza", "Pizza", R.drawable.placeholder_image),
    Category("salad", "Salad", R.drawable.placeholder_image),
    Category("dessert", "Dessert", R.drawable.placeholder_image),
    Category("soup", "Soup", R.drawable.placeholder_image)
)

val recommendedRecipes = listOf(
    Recipe(
        id = "1",
        title = "Spaghetti Carbonara",
        author = "Italian Chef",
        imageUrl = R.drawable.placeholder_image,
        categories = listOf("pasta", "italian")
    ),
    Recipe(
        id = "2",
        title = "Chicken Curry",
        author = "Indian Foodie",
        imageUrl = R.drawable.placeholder_image,
        categories = listOf("chicken", "curry")
    ),
    Recipe(
        id = "3",
        title = "Margherita Pizza",
        author = "Pizza Expert",
        imageUrl = R.drawable.placeholder_image,
        categories = listOf("pizza", "italian")
    ),
    Recipe(
        id = "4",
        title = "Caesar Salad",
        author = "Salad Master",
        imageUrl = R.drawable.placeholder_image,
        categories = listOf("salad", "healthy")
    )
) 
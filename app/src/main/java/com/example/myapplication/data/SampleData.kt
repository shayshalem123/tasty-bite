package com.example.myapplication.data

import com.example.myapplication.R
import com.example.myapplication.models.Category
import com.example.myapplication.models.Ingredient
import com.example.myapplication.models.Recipe

// Categories
val categories = listOf(
    Category("pasta", "Pasta", R.drawable.placeholder_image),
    Category("pizza", "Pizza", R.drawable.placeholder_image),
    Category("salad", "Salad", R.drawable.placeholder_image),
    Category("dessert", "Dessert", R.drawable.placeholder_image),
    Category("soup", "Soup", R.drawable.placeholder_image),
    Category("chicken", "Chicken", R.drawable.placeholder_image)
)

// Recipes
val recommendedRecipes = listOf(
    Recipe(
        id = "1",
        title = "Spaghetti Carbonara",
        author = "Jamie Oliver",
        imageUrl = R.drawable.placeholder_image,
        categories = listOf("pasta"),
        description = "A classic Italian pasta dish made with eggs, cheese, pancetta, and black pepper. Rich, creamy, and delicious.",
        cookingTime = "20 mins",
        difficulty = "Medium",
        calories = "650 cal",
        ingredients = listOf(
            Ingredient("Spaghetti", "400 g", R.drawable.placeholder_image),
            Ingredient("Pancetta", "150 g", R.drawable.placeholder_image),
            Ingredient("Egg Yolks", "4 items", R.drawable.placeholder_image),
            Ingredient("Parmesan Cheese", "50 g", R.drawable.placeholder_image),
            Ingredient("Black Pepper", "5 g", R.drawable.placeholder_image),
            Ingredient("Salt", "3 g", R.drawable.placeholder_image)
        ),
        instructions = listOf(
            "Boil the spaghetti",
            "Cook the pancetta",
            "Mix eggs and cheese",
            "Combine with pasta",
            "Serve hot"
        ),
        cookTime = 20,
        servings = 4,
        category = "pasta"
    ),
    Recipe(
        id = "2",
        title = "Margherita Pizza",
        author = "Gordon Ramsay",
        imageUrl = R.drawable.placeholder_image,
        categories = listOf("pizza"),
        description = "A simple yet delicious pizza topped with tomatoes, mozzarella cheese, fresh basil, and extra virgin olive oil.",
        cookingTime = "30 mins",
        difficulty = "Easy",
        calories = "800 cal",
        ingredients = listOf(
            Ingredient("Pizza Dough", "250 g", R.drawable.placeholder_image),
            Ingredient("Tomato Sauce", "100 g", R.drawable.placeholder_image),
            Ingredient("Mozzarella Cheese", "150 g", R.drawable.placeholder_image),
            Ingredient("Fresh Basil", "10 g", R.drawable.placeholder_image),
            Ingredient("Olive Oil", "15 g", R.drawable.placeholder_image),
            Ingredient("Salt", "2 g", R.drawable.placeholder_image)
        ),
        instructions = listOf(
            "Spread tomato sauce on dough",
            "Add mozzarella cheese",
            "Top with fresh basil",
            "Bake in oven",
            "Serve hot"
        ),
        cookTime = 30,
        servings = 8,
        category = "pizza"
    ),
    Recipe(
        id = "3",
        title = "Caesar Salad",
        author = "Nigella Lawson",
        imageUrl = R.drawable.placeholder_image,
        categories = listOf("salad"),
        description = "A green salad with romaine lettuce and croutons dressed with lemon juice, olive oil, egg, Worcestershire sauce, anchovies, garlic, Dijon mustard, Parmesan cheese, and black pepper.",
        cookingTime = "15 mins",
        difficulty = "Easy",
        calories = "350 cal",
        ingredients = listOf(
            Ingredient("Romaine Lettuce", "300 g", R.drawable.placeholder_image),
            Ingredient("Croutons", "50 g", R.drawable.placeholder_image),
            Ingredient("Parmesan Cheese", "30 g", R.drawable.placeholder_image),
            Ingredient("Caesar Dressing", "60 g", R.drawable.placeholder_image),
            Ingredient("Chicken Breast", "200 g", R.drawable.placeholder_image),
            Ingredient("Cherry Tomatoes", "100 g", R.drawable.placeholder_image)
        ),
        instructions = listOf(
            "Chop romaine lettuce",
            "Add croutons",
            "Dress with Caesar dressing",
            "Add chicken and tomatoes",
            "Serve"
        ),
        cookTime = 15,
        servings = 4,
        category = "salad"
    ),
    Recipe(
        id = "4",
        title = "Chocolate Cake",
        author = "Mary Berry",
        imageUrl = R.drawable.placeholder_image,
        categories = listOf("dessert"),
        description = "A rich and moist chocolate cake with a velvety chocolate frosting. Perfect for celebrations or as a special treat.",
        cookingTime = "45 mins",
        difficulty = "Medium",
        calories = "420 cal",
        ingredients = listOf(
            Ingredient("All-Purpose Flour", "250 g", R.drawable.placeholder_image),
            Ingredient("Cocoa Powder", "75 g", R.drawable.placeholder_image),
            Ingredient("Sugar", "200 g", R.drawable.placeholder_image),
            Ingredient("Eggs", "3 items", R.drawable.placeholder_image),
            Ingredient("Butter", "150 g", R.drawable.placeholder_image),
            Ingredient("Milk", "120 g", R.drawable.placeholder_image),
            Ingredient("Vanilla Extract", "5 g", R.drawable.placeholder_image),
            Ingredient("Baking Powder", "10 g", R.drawable.placeholder_image)
        ),
        instructions = listOf(
            "Preheat oven to 350°F (175°C)",
            "Mix dry ingredients",
            "Add eggs, butter, and milk",
            "Bake for 45 minutes",
            "Cool and frost"
        ),
        cookTime = 45,
        servings = 8,
        category = "dessert"
    ),
    Recipe(
        id = "5",
        title = "Chicken Curry",
        author = "Vikas Khanna",
        imageUrl = R.drawable.placeholder_image,
        categories = listOf("chicken"),
        description = "A flavorful chicken curry made with aromatic spices, onions, tomatoes, and fresh herbs. Serve with rice or naan bread.",
        cookingTime = "40 mins",
        difficulty = "Medium",
        calories = "580 cal",
        ingredients = listOf(
            Ingredient("Chicken Thighs", "500 g", R.drawable.placeholder_image),
            Ingredient("Onions", "2 items", R.drawable.placeholder_image),
            Ingredient("Tomatoes", "3 items", R.drawable.placeholder_image),
            Ingredient("Ginger", "20 g", R.drawable.placeholder_image),
            Ingredient("Garlic", "15 g", R.drawable.placeholder_image),
            Ingredient("Curry Powder", "30 g", R.drawable.placeholder_image),
            Ingredient("Coconut Milk", "200 g", R.drawable.placeholder_image),
            Ingredient("Vegetable Oil", "30 g", R.drawable.placeholder_image),
            Ingredient("Fresh Cilantro", "10 g", R.drawable.placeholder_image)
        ),
        instructions = listOf(
            "Sauté onions and ginger",
            "Add tomatoes and curry powder",
            "Cook until tomatoes are soft",
            "Add coconut milk and chicken",
            "Simmer until chicken is cooked"
        ),
        cookTime = 40,
        servings = 4,
        category = "chicken"
    ),
    Recipe(
        id = "6",
        title = "Tomato Soup",
        author = "Julia Child",
        imageUrl = R.drawable.placeholder_image,
        categories = listOf("soup"),
        description = "A comforting classic made with ripe tomatoes, aromatic herbs, and a touch of cream. Perfect for cold days or paired with a grilled cheese sandwich.",
        cookingTime = "30 mins",
        difficulty = "Easy",
        calories = "220 cal",
        ingredients = listOf(
            Ingredient("Ripe Tomatoes", "1 kg", R.drawable.placeholder_image),
            Ingredient("Onion", "1 items", R.drawable.placeholder_image),
            Ingredient("Garlic", "2 items", R.drawable.placeholder_image),
            Ingredient("Vegetable Broth", "750 g", R.drawable.placeholder_image),
            Ingredient("Fresh Basil", "20 g", R.drawable.placeholder_image),
            Ingredient("Heavy Cream", "100 g", R.drawable.placeholder_image),
            Ingredient("Olive Oil", "30 g", R.drawable.placeholder_image),
            Ingredient("Salt and Pepper", "5 g", R.drawable.placeholder_image)
        ),
        instructions = listOf(
            "Sauté onions and garlic",
            "Add tomatoes and vegetable broth",
            "Simmer until tomatoes are soft",
            "Add fresh basil",
            "Blend and add heavy cream"
        ),
        cookTime = 30,
        servings = 4,
        category = "soup"
    )
) 
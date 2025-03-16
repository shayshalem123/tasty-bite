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
        description = "A classic Italian dish that's quick and easy to make. Creamy eggs, savory pancetta, and sharp Pecorino Romano cheese combine to create a silky sauce that clings to every strand of spaghetti.",
        cookingTime = "20 mins",
        difficulty = "Easy",
        calories = "650 cal",
        ingredients = listOf(
            Ingredient("Spaghetti", "400g", R.drawable.placeholder_image),
            Ingredient("Pancetta", "150g", R.drawable.placeholder_image),
            Ingredient("Eggs", "4", R.drawable.placeholder_image),
            Ingredient("Pecorino Romano", "50g", R.drawable.placeholder_image),
            Ingredient("Black Pepper", "to taste", R.drawable.placeholder_image)
        )
    ),
    Recipe(
        id = "2",
        title = "Margherita Pizza",
        author = "Gordon Ramsay",
        imageUrl = R.drawable.placeholder_image,
        categories = listOf("pizza"),
        description = "The classic Margherita pizza represents the colors of the Italian flag with red tomatoes, white mozzarella, and green basil. Simple yet incredibly flavorful.",
        cookingTime = "25 mins",
        difficulty = "Medium",
        calories = "850 cal",
        ingredients = listOf(
            Ingredient("Pizza Dough", "1", R.drawable.placeholder_image),
            Ingredient("San Marzano Tomatoes", "400g", R.drawable.placeholder_image),
            Ingredient("Fresh Mozzarella", "200g", R.drawable.placeholder_image),
            Ingredient("Fresh Basil", "10 leaves", R.drawable.placeholder_image),
            Ingredient("Extra Virgin Olive Oil", "2 tbsp", R.drawable.placeholder_image),
            Ingredient("Salt", "to taste", R.drawable.placeholder_image)
        )
    ),
    Recipe(
        id = "3",
        title = "Greek Salad",
        author = "Ina Garten",
        imageUrl = R.drawable.placeholder_image,
        categories = listOf("salad"),
        description = "A refreshing Mediterranean salad featuring crisp vegetables, tangy feta cheese, and Kalamata olives, all dressed with olive oil and lemon juice.",
        cookingTime = "15 mins",
        difficulty = "Easy",
        calories = "320 cal",
        ingredients = listOf(
            Ingredient("Cucumber", "1 large", R.drawable.placeholder_image),
            Ingredient("Tomatoes", "4 medium", R.drawable.placeholder_image),
            Ingredient("Red Onion", "1/2", R.drawable.placeholder_image),
            Ingredient("Green Bell Pepper", "1", R.drawable.placeholder_image),
            Ingredient("Kalamata Olives", "1/2 cup", R.drawable.placeholder_image),
            Ingredient("Feta Cheese", "200g", R.drawable.placeholder_image),
            Ingredient("Extra Virgin Olive Oil", "1/4 cup", R.drawable.placeholder_image),
            Ingredient("Lemon Juice", "2 tbsp", R.drawable.placeholder_image),
            Ingredient("Dried Oregano", "1 tsp", R.drawable.placeholder_image)
        )
    ),
    Recipe(
        id = "4",
        title = "Chocolate Macarons",
        author = "Rachel William",
        imageUrl = R.drawable.placeholder_image,
        categories = listOf("dessert"),
        description = "These elegant French confections feature crisp shells with a chewy interior and rich chocolate ganache filling. Perfect for special occasions or an indulgent treat.",
        cookingTime = "45 mins",
        difficulty = "Hard",
        calories = "380 cal",
        ingredients = listOf(
            Ingredient("Almond Flour", "100g", R.drawable.placeholder_image),
            Ingredient("Powdered Sugar", "170g", R.drawable.placeholder_image),
            Ingredient("Egg Whites", "100g", R.drawable.placeholder_image),
            Ingredient("Granulated Sugar", "50g", R.drawable.placeholder_image),
            Ingredient("Cocoa Powder", "15g", R.drawable.placeholder_image),
            Ingredient("Dark Chocolate", "100g", R.drawable.placeholder_image),
            Ingredient("Heavy Cream", "100ml", R.drawable.placeholder_image)
        )
    ),
    Recipe(
        id = "5",
        title = "Chicken Curry",
        author = "Madhur Jaffrey",
        imageUrl = R.drawable.placeholder_image,
        categories = listOf("chicken"),
        description = "A flavorful Indian curry with tender chicken pieces in a rich, aromatic sauce. Serve with rice or naan bread for a complete meal.",
        cookingTime = "40 mins",
        difficulty = "Medium",
        calories = "420 cal",
        ingredients = listOf(
            Ingredient("Chicken Thighs", "500g", R.drawable.placeholder_image),
            Ingredient("Onion", "2 large", R.drawable.placeholder_image),
            Ingredient("Garlic", "4 cloves", R.drawable.placeholder_image),
            Ingredient("Ginger", "2-inch piece", R.drawable.placeholder_image),
            Ingredient("Tomatoes", "3 medium", R.drawable.placeholder_image),
            Ingredient("Curry Powder", "2 tbsp", R.drawable.placeholder_image),
            Ingredient("Garam Masala", "1 tsp", R.drawable.placeholder_image),
            Ingredient("Turmeric", "1 tsp", R.drawable.placeholder_image),
            Ingredient("Coconut Milk", "400ml", R.drawable.placeholder_image),
            Ingredient("Cilantro", "for garnish", R.drawable.placeholder_image)
        )
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
            Ingredient("Ripe Tomatoes", "1kg", R.drawable.placeholder_image),
            Ingredient("Onion", "1 medium", R.drawable.placeholder_image),
            Ingredient("Garlic", "2 cloves", R.drawable.placeholder_image),
            Ingredient("Vegetable Broth", "750ml", R.drawable.placeholder_image),
            Ingredient("Fresh Basil", "1 handful", R.drawable.placeholder_image),
            Ingredient("Heavy Cream", "100ml", R.drawable.placeholder_image),
            Ingredient("Olive Oil", "2 tbsp", R.drawable.placeholder_image),
            Ingredient("Salt and Pepper", "to taste", R.drawable.placeholder_image)
        )
    )
) 
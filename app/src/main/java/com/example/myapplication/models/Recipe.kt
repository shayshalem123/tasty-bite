package com.example.myapplication.models

data class Recipe(
    val id: String = "",
    val title: String,
    val author: String,
    val imageUrl: String, // Using resource ID for now, would be URL in real app
    val categories: List<String>? = null, // Category IDs
    val description: String? = null,
    val cookingTime: String? = null,
    val difficulty: String? = null,
    val calories: String? = null,
    val ingredients: List<Ingredient>? = null,
    val instructions: List<String>,
    val cookTime: Int,
    val servings: Int,
    val category: String,
    val isFavorite: Boolean = false,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Category(
    val id: String,
    val name: String,
    val icon: Int
)

data class Ingredient(
    val name: String,
    val amount: String,
    val imageUrl: Int? = null
) 
package com.example.myapplication.data.model

// Model for ingredient search response from Spoonacular API
data class IngredientSearchResponse(
    val results: List<IngredientResult> = emptyList(),
    val offset: Int = 0,
    val number: Int = 0,
    val totalResults: Int = 0
)

// Model for individual ingredient result
data class IngredientResult(
    val id: Int = 0,
    val name: String = "",
    val image: String = ""
) 
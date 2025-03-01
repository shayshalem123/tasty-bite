package com.example.myapplication.models

data class Recipe(
    val id: String,
    val title: String,
    val author: String,
    val imageUrl: Int // Using resource ID for now, would be URL in real app
)

data class Category(
    val id: String,
    val name: String,
    val icon: Int
) 
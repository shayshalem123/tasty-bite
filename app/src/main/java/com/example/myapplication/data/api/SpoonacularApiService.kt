package com.example.myapplication.data.api

import com.example.myapplication.data.model.IngredientResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import org.json.JSONObject

/**
 * Service class for interacting with the Spoonacular API
 */
class SpoonacularApiService(
) {
    private val client: OkHttpClient = OkHttpClient()
    private val baseUrl = "https://api.spoonacular.com"
    private val apikey = "44b788577b74431e94e9dcfb5c25b004"

    suspend fun searchIngredients(
        query: String, 
        number: Int = 10
    ): List<IngredientResult> = withContext(Dispatchers.IO) {
//        return@withContext mockIngredients()

        if (query.isEmpty()) {
            return@withContext emptyList()
        }

        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "$baseUrl/food/ingredients/search?query=$encodedQuery&number=$number"

            val request = Request.Builder()
                .url(url)
                .addHeader("x-api-key", apikey)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: return@withContext emptyList()

                // Parse the JSON response manually
                val jsonResponse = JSONObject(responseBody)
                val resultsArray = jsonResponse.getJSONArray("results")

                val ingredients = mutableListOf<IngredientResult>()

                // Parse each ingredient in the array
                for (i in 0 until resultsArray.length()) {
                    val ingredientJson = resultsArray.getJSONObject(i)
                    val id = ingredientJson.optInt("id", 0)
                    val name = ingredientJson.optString("name", "")
                    val image = ingredientJson.optString("image", "")

                    ingredients.add(IngredientResult(id, name, image))
                }

                return@withContext ingredients
            } else {
                return@withContext emptyList()
            }
        } catch (e: Exception) {
            return@withContext emptyList()
        }
    }

    fun mockIngredients(): List<IngredientResult> {
        val ingredients2 = mutableListOf<IngredientResult>()

        ingredients2.add(IngredientResult(1, "pasta", "pasta.png"))
        ingredients2.add(IngredientResult(2, "garlic", "garlic.png"))
        ingredients2.add(IngredientResult(3, "egg", "egg.png"))
        ingredients2.add(IngredientResult(3, "eggplant", "eggplant.png"))

        return ingredients2
    }
} 
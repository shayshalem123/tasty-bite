package com.example.myapplication.ui.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.api.SpoonacularApiService
import com.example.myapplication.data.model.IngredientResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class IngredientSearchViewModel(
    private val apiService: SpoonacularApiService = SpoonacularApiService()
) : ViewModel() {
    
    // State for ingredient suggestions
    private val _suggestions = MutableStateFlow<List<IngredientResult>>(emptyList())
    val suggestions: StateFlow<List<IngredientResult>> = _suggestions
    
    // State for loading indicator
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // Counter to help prevent race conditions
    private val requestCounter = AtomicInteger(0)
    
    /**
     * Search for ingredients based on user input
     */
    fun searchIngredients(query: String) {
        if (query.length < 2) {
            _suggestions.value = emptyList()
            return
        }
        
        _isLoading.value = true
        val currentRequest = requestCounter.incrementAndGet()
        
        viewModelScope.launch {
            try {
                val results = apiService.searchIngredients(query)
                
                // Only update if this is the most recent request
                if (currentRequest == requestCounter.get()) {
                    _suggestions.value = results
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                if (currentRequest == requestCounter.get()) {
                    _suggestions.value = emptyList()
                    _isLoading.value = false
                }
            }
        }
    }
    
    /**
     * Clear current suggestions
     */
    fun clearSuggestions() {
        _suggestions.value = emptyList()
    }
} 
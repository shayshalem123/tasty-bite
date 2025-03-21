package com.example.myapplication.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Utility class for caching profile images to improve performance
 * and reduce network calls
 */
object ProfileImageCache {
    private const val TAG = "ProfileImageCache"
    
    // Cache of profile image URLs to their corresponding StateFlow
    private val imageCache = mutableMapOf<String, MutableStateFlow<Drawable?>>()
    
    /**
     * Get a StateFlow for a profile image URL
     * @param context Android context
     * @param imageUrl URL of the profile image
     * @return StateFlow containing the image Drawable or null
     */
    fun getProfileImage(context: Context, imageUrl: String): StateFlow<Drawable?> {
        if (!imageCache.containsKey(imageUrl)) {
            imageCache[imageUrl] = MutableStateFlow(null)
            loadProfileImage(context, imageUrl)
        }
        
        return imageCache[imageUrl]!!
    }
    
    /**
     * Load a profile image from the given URL
     * @param context Android context
     * @param imageUrl URL of the profile image
     */
    private fun loadProfileImage(context: Context, imageUrl: String) {
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .listener(
                onSuccess = { _, result ->
                    imageCache[imageUrl]?.value = result.image as? Drawable
                    Log.d(TAG, "Loaded profile image: $imageUrl")
                },
                onError = { _, error ->
                    Log.e(TAG, "Failed to load profile image: $imageUrl", error.throwable)
                }
            )
            .build()
        
        context.imageLoader.enqueue(request)
    }
    
    /**
     * Invalidate the cache for a specific image URL
     * @param context Android context
     * @param imageUrl URL of the profile image to invalidate
     */
    fun invalidateCache(context: Context, imageUrl: String) {
        imageCache.remove(imageUrl)
        if (imageUrl.isNotEmpty()) {
            loadProfileImage(context, imageUrl)
        }
    }
    
    /**
     * Clear the entire cache
     */
    fun clearCache() {
        imageCache.clear()
    }
} 
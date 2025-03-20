package com.example.myapplication.data

import android.net.Uri
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Service for managing recipe images in Firebase Storage
 */
class RecipesImageRepository {
    private val TAG = "FirebaseStorageService"

    private val storage = Firebase.storage("gs://tasty-bite-19b53.firebasestorage.app")
    private val storageRef = storage.reference;

    suspend fun uploadImage(
        imageUri: Uri,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting image upload for URI: $imageUri")

            val filename = "recipe_images/${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child(filename)

            imageRef.putFile(imageUri).await()

            Log.d(TAG, "Upload completed successfully")

            val downloadUrl = imageRef.downloadUrl.await().toString()
            Log.d(TAG, "Download URL: $downloadUrl")

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image", e)
            Result.failure(e)
        }
    }

    /**
     * Gets the download URL for an image from Firebase Storage
     * 
     * @param imageUrl The path of the image in Firebase Storage
     * @return Result containing the download URL as a String or an error
     */
    suspend fun getImageUrl(imageUrl: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // If the URL already looks like a valid http(s) URL, return it directly
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                return@withContext Result.success(imageUrl)
            }
            
            // Otherwise, treat it as a reference path in Firebase Storage
            val imageRef = storageRef.child(imageUrl)
            val downloadUrl = imageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting download URL for $imageUrl", e)
            Result.failure(e)
        }
    }
} 
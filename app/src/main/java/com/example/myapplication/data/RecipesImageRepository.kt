package com.example.myapplication.data

import android.content.Context
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
class FirebaseStorageService {
    private val TAG = "FirebaseStorageService"
    
    // Initialize Firebase Storage
    private val storage = Firebase.storage("gs://tasty-bite-19b53.firebasestorage.app")

    private val storageRef = storage.reference;
    
    // Add a field to track the current upload task
    private var currentUploadTask: UploadTask? = null
    
    /**
     * Uploads an image to Firebase Storage and returns the download URL
     *
     * @param context Android context for content resolver access
     * @param imageUri URI of the image to upload
     * @param onProgress Callback for progress updates (0-100)
     * @return Result containing the download URL as a String or an error
     */
    suspend fun uploadImage(
        context: Context, 
        imageUri: Uri,
        onProgress: (Int) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting image upload for URI: $imageUri")

            // Create a unique filename using UUID
            val filename = "recipe_images/${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child(filename)
            
            // Upload the file
            val uploadTask = imageRef.putFile(imageUri)
            currentUploadTask = uploadTask
            
            // Set up progress listener
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }
            
            // Wait for upload to complete
            uploadTask.await()
            Log.d(TAG, "Upload completed successfully")
            
            // Get the download URL
            val downloadUrl = imageRef.downloadUrl.await().toString()
            Log.d(TAG, "Download URL: $downloadUrl")
            
            onProgress(100)
            currentUploadTask = null
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image", e)
            onProgress(0) // Reset progress
            currentUploadTask = null
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

    // Cancel current upload
    fun cancelUpload() {
        currentUploadTask?.cancel()
        currentUploadTask = null
    }
} 
package com.example.myapplication.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
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

            val image = storageRef.child("images/mountains.jpg")
            
            // Get input stream from URI
            image.putFile(imageUri).await()
            Log.d(TAG, "Upload completed successfully")
            
            // Get the download
            
            Result.success("hello")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image", e)
            onProgress(0) // Reset progress
            Result.failure(e)
        }
    }

    // Add this function to the FirebaseStorageService class
    fun cancelUpload() {
        currentUploadTask?.cancel()
        currentUploadTask = null
    }
} 
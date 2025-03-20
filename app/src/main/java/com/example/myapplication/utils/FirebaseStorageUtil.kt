package com.example.myapplication.utils

import android.net.Uri
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FirebaseStorageUtil {
    private const val TAG = "FirebaseStorageUtil"
    
    // Reference to your custom bucket with KTX syntax
    private val storage = Firebase.storage("gs://tasty-bite-19b53.firebasestorage.app")
    private val storageRef = storage.reference
    
    /**
     * Upload recipe image to Firebase Storage
     * @param imageUri Uri of the image to upload
     * @return Result with the download URL or error
     */
    suspend fun uploadImage(imageUri: Uri): Result<String> {
        return try {
            val filename = "recipe_${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child("recipe_images/$filename")
            
            Log.d(TAG, "Starting image upload: $filename")
            val uploadTask = imageRef.putFile(imageUri).await()
            Log.d(TAG, "Image uploaded successfully")
            
            // Get download URL
            val downloadUrl = imageRef.downloadUrl.await().toString()
            Log.d(TAG, "Download URL: $downloadUrl")
            
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload image", e)
            Result.failure(e)
        }
    }
    
    /**
     * Test the Firebase Storage connection
     * @return Result with success or failure
     */
    suspend fun testConnection(): Result<Boolean> {
        return try {
            val testRef = storageRef.child("test/connection_test.txt")
            val testData = "Connection test at ${System.currentTimeMillis()}".toByteArray()
            
            Log.d(TAG, "Testing Storage connection...")
            testRef.putBytes(testData).await()
            Log.d(TAG, "Storage connection successful")
            
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Storage connection failed", e)
            Result.failure(e)
        }
    }
} 
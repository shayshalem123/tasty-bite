package com.example.myapplication.auth

import android.net.Uri
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository for managing user profile images in Firebase Storage
 * Handles uploading and retrieving profile images
 */
class ProfileImageRepository {
    private val TAG = "ProfileImageRepository"
    
    // Firebase Storage reference - using the same bucket as recipe images
    private val storage = Firebase.storage("gs://tasty-bite-19b53.firebasestorage.app")
    private val storageRef = storage.reference
    
    /**
     * Upload a profile image to Firebase Storage
     * @param imageUri URI of the image to upload
     * @param userId User ID to associate with the image
     * @return Result with the download URL or error
     */
    suspend fun uploadProfileImage(imageUri: Uri, userId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting profile image upload for user: $userId")
            
            // Create a unique filename with user ID
            val filename = "profile_$userId.jpg"
            val imageRef = storageRef.child("profile_images/$filename")
            
            // Upload the file
            imageRef.putFile(imageUri).await()
            Log.d(TAG, "Profile image uploaded successfully")
            
            // Get the download URL
            val downloadUrl = imageRef.downloadUrl.await().toString()
            Log.d(TAG, "Profile image download URL: $downloadUrl")
            
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile image", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get the download URL for a profile image
     * @param imageUrl The path of the image in Firebase Storage
     * @return Result containing the download URL as a String or an error
     */
    suspend fun getProfileImageUrl(imageUrl: String): Result<String> = withContext(Dispatchers.IO) {
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
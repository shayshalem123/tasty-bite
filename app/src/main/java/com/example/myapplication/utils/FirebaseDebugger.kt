package com.example.myapplication.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseDebugger {
    private const val TAG = "FirebaseDebugger"
    
    /**
     * Tests Firebase Firestore connectivity
     * @return true if connection works, false otherwise
     */
    suspend fun testFirestoreConnection(): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            val testDoc = db.collection("debug").document("connectivity_test")
            
            // Try to write
            val testData = mapOf(
                "timestamp" to System.currentTimeMillis(),
                "test" to "connectivity"
            )
            
            Log.d(TAG, "Attempting to write test data to Firestore")
            testDoc.set(testData).await()
            Log.d(TAG, "Successfully wrote test data to Firestore")
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to Firestore", e)
            false
        }
    }
} 
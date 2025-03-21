package com.example.myapplication.ui.screens.add

sealed class SaveState {
    object Initial : SaveState()
    object Saving : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

sealed class UploadState {
    object Idle : UploadState()
    object Uploading : UploadState()
    data class Error(val message: String) : UploadState()
} 
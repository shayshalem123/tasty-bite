package com.example.myapplication.ui.screens.add.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NumericFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    suffix: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    // State to track invalid input attempts
    var showInvalidInputError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Only accept digits
            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            } else {
                // Show brief error message for invalid input
                showInvalidInputError = true
                scope.launch {
                    delay(2000) // Show error for 2 seconds
                    showInvalidInputError = false
                }
            }
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        suffix = { Text(suffix) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        isError = isError || showInvalidInputError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        supportingText = when {
            showInvalidInputError -> {
                { Text("Only numbers allowed", color = MaterialTheme.colorScheme.error) }
            }
            isError -> {
                { Text("This field is required") }
            }
            else -> null
        }
    )
} 
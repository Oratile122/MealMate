package com.example.mealmate.api

// AuthResponse - data class that represents the response from the API
// after a successful (or failed) login or registration request
// Retrofit automatically converts the JSON response into this object

data class AuthResponse(
    val message: String,              // Status message (e.g. "Login successful")
    val userId: Int? = null,          // User's ID from the database (nullable)
    val fullName: String? = null,     // User's full name (nullable)
    val email: String? = null         // User's email address (nullable)
)
package com.example.mealmate.api

// RegisterRequest - data class that represents the data sent to the API
// when a new user registers. Retrofit converts this object into JSON
// and sends it in the POST request body.

data class RegisterRequest(
    val fullName: String,   // The user's full name
    val email: String,      // The user's email address
    val password: String    // The user's password
)
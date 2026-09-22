package com.example.mealmate.api

// LoginRequest - data class that represents the data sent to the API
// when a user logs in. Retrofit converts this object into JSON
// and sends it in the POST request body.

data class LoginRequest(
    val email: String,      // The user's email address
    val password: String    // The user's password
)
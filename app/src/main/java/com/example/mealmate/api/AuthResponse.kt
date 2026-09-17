package com.example.mealmate.api

data class AuthResponse(
    val message: String,
    val userId: Int? = null,
    val fullName: String? = null,
    val email: String? = null
)
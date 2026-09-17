package com.example.mealmate.api

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String
)
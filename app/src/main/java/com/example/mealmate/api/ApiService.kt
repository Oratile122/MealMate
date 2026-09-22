package com.example.mealmate.api

// ApiService - Retrofit interface that defines the REST API endpoints
// Used to communicate with the .NET backend server

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // Registers a new user
    // POST request to: {BASE_URL}/register
    // Sends: RegisterRequest (name, email, password)
    // Returns: AuthResponse (success/failure)
    @POST("register")
    fun register(
        @Body request: RegisterRequest
    ): Call<AuthResponse>

    // Logs in an existing user
    // POST request to: {BASE_URL}/login
    // Sends: LoginRequest (email, password)
    // Returns: AuthResponse (success/failure + token)
    @POST("login")
    fun login(
        @Body request: LoginRequest
    ): Call<AuthResponse>
}
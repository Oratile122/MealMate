package com.example.mealmate.api

// RetrofitClient - singleton object that creates the Retrofit instance
// Retrofit is used to make HTTP requests to the REST API
// This client is used by all Activities that need to talk to the server

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {


    private const val BASE_URL = "http://10.0.0.4:5185/api/Auth/"

    // Lazily-created Retrofit service instance
    // Created once and reused for all API calls
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)                                        // Set the base URL
            .addConverterFactory(GsonConverterFactory.create())       // Use Gson to convert JSON ↔ Kotlin objects
            .build()
            .create(ApiService::class.java)                           // Create the ApiService implementation
    }
}
package com.example.mealmate.database

// UserEntity - Room entity representing the "users" table
// Each UserEntity = one registered user of the app
// Stores profile info, preferences, and login status

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity annotation tells Room to create a table called "users"
@Entity(tableName = "users")
data class UserEntity(

    // Primary key - auto-incremented by Room (0 = "not set yet")
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // User's email address (used as the login identifier)
    val email: String,

    // User's display name (shown on Home screen)
    val displayName: String,

    // User's password (hashed on the backend, stored locally for offline login)
    val password: String,

    // Weekly food budget in Rands - defaults to R 500
    val budget: Double = 500.0,

    // Dietary preference - defaults to "None"
    // Options: "None", "Vegetarian", "Vegan", "Gluten-Free"
    val dietaryPreference: String = "None",

    // Preferred language - defaults to "English"
    // Options: "English", "IsiZulu", "Afrikaans"
    val language: String = "English",

    // Whether push notifications are enabled - defaults to true
    val notifications: Boolean = true,

    // Whether this user is currently logged in
    // Only one user can be logged in at a time
    val isLoggedIn: Boolean = false
)
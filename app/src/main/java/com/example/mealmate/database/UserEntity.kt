package com.example.mealmate.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val email: String,
    val displayName: String,
    val password: String,
    val budget: Double = 500.0,
    val dietaryPreference: String = "None",
    val language: String = "English",
    val notifications: Boolean = true,
    val isLoggedIn: Boolean = false
)
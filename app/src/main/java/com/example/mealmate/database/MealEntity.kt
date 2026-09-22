package com.example.mealmate.database

// MealEntity - Room entity representing the "meals" table in the database
// Each MealEntity object = one row in the meals table
// Stores info about a meal a user has planned (name, cost, calories, etc.)

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity annotation tells Room to create a table called "meals"
@Entity(tableName = "meals")
data class MealEntity(

    // Primary key - auto-incremented by Room (0 means "not set yet")
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Email of the user this meal belongs to
    // Used to filter meals per user (offline multi-user support)
    val userEmail: String,

    // Name of the meal (e.g. "Oat Porridge")
    val name: String,

    // Category - one of "Breakfast", "Lunch", "Dinner", or "Snack"
    val category: String,

    // Comma-separated list of ingredients (e.g. "Oats, Milk, Banana")
    val ingredients: String,

    // Cost of the meal in Rands
    val cost: Double,

    // Calorie count (optional, defaults to 0)
    val calories: Int = 0,

    // Day of the week (e.g. "Monday") - currently unused, reserved for future
    val day: String = "",

    // Same as category - used for filtering meals by time of day
    val mealTime: String = ""
)
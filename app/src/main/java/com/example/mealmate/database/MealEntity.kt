package com.example.mealmate.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userEmail: String,
    val name: String,
    val category: String,
    val ingredients: String,
    val cost: Double,
    val calories: Int = 0,
    val day: String = "",
    val mealTime: String = ""
)
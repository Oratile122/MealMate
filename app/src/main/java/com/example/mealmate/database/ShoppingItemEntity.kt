package com.example.mealmate.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey
    val itemName: String,
    val userEmail: String,
    val isChecked: Boolean = false,
    val price: Double = 0.0
)
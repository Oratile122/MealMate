package com.example.mealmate.database

// ShoppingItemEntity - Room entity representing the "shopping_items" table
// Each ShoppingItemEntity = one row in the table
// Stores whether a specific shopping item is checked, per user

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity annotation tells Room to create a table called "shopping_items"
@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(

    // Primary key - the item's name is unique enough to be the ID
    @PrimaryKey
    val itemName: String,          // e.g. "Oats", "Milk"

    // Email of the user this shopping item belongs to
    // Scopes items per user so different users have separate lists
    val userEmail: String,

    // Whether the item is checked (bought) - defaults to false
    val isChecked: Boolean = false,

    // Price of the item in Rands - defaults to 0.0
    val price: Double = 0.0
)
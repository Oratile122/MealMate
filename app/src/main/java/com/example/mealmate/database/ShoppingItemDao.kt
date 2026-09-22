package com.example.mealmate.database

// ShoppingItemDao - Data Access Object for the "shopping_items" table
// Room generates the implementation of this interface automatically
// Used by ShoppingListActivity to persist checkbox states per user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ShoppingItemDao {

    // Inserts a new shopping item or replaces an existing one
    // Called when a user checks an item for the first time
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity)

    // Returns all shopping items for a specific user
    // (Currently unused but available for future features)
    @Query("SELECT * FROM shopping_items WHERE userEmail = :email")
    suspend fun getItemsByUser(email: String): List<ShoppingItemEntity>

    // Checks if a specific item is checked for a user
    // Returns true/false, or null if the item has never been saved
    @Query("SELECT isChecked FROM shopping_items WHERE userEmail = :email AND itemName = :itemName LIMIT 1")
    suspend fun isItemChecked(email: String, itemName: String): Boolean?

    // Updates the checked state of an existing item
    // Called when a user toggles a checkbox
    @Query("UPDATE shopping_items SET isChecked = :checked WHERE userEmail = :email AND itemName = :itemName")
    suspend fun updateChecked(email: String, itemName: String, checked: Boolean)

    // Unchecks all items for a user (used by the "Clear List" button)
    @Query("UPDATE shopping_items SET isChecked = 0 WHERE userEmail = :email")
    suspend fun uncheckAll(email: String)

    // Deletes all shopping items for a user
    // (Currently unused but available for future features)
    @Query("DELETE FROM shopping_items WHERE userEmail = :email")
    suspend fun deleteAllForUser(email: String)
}
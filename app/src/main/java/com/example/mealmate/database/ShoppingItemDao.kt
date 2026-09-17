package com.example.mealmate.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ShoppingItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingItemEntity)

    @Query("SELECT * FROM shopping_items WHERE userEmail = :email")
    suspend fun getItemsByUser(email: String): List<ShoppingItemEntity>

    @Query("SELECT isChecked FROM shopping_items WHERE userEmail = :email AND itemName = :itemName LIMIT 1")
    suspend fun isItemChecked(email: String, itemName: String): Boolean?

    @Query("UPDATE shopping_items SET isChecked = :checked WHERE userEmail = :email AND itemName = :itemName")
    suspend fun updateChecked(email: String, itemName: String, checked: Boolean)

    @Query("UPDATE shopping_items SET isChecked = 0 WHERE userEmail = :email")
    suspend fun uncheckAll(email: String)

    @Query("DELETE FROM shopping_items WHERE userEmail = :email")
    suspend fun deleteAllForUser(email: String)
}
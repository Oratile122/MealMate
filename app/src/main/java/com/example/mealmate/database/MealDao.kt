package com.example.mealmate.database

// MealDao - Data Access Object for the "meals" table
// Room generates the implementation of this interface automatically
// All functions are suspend (async) so they run on a background thread

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MealDao {

    // Inserts a new meal into the database
    // REPLACE strategy means if a meal with the same ID exists, it gets overwritten
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    // Returns all meals for a specific user (by email)
    // Used by HomeActivity and WeeklyPlanActivity
    @Query("SELECT * FROM meals WHERE userEmail = :email")
    suspend fun getMealsByUser(email: String): List<MealEntity>

    // Returns one meal for a specific user matching a mealTime (e.g. "Breakfast")
    // Returns null if no match found
    @Query("SELECT * FROM meals WHERE userEmail = :email AND mealTime = :mealTime LIMIT 1")
    suspend fun getMealByTime(email: String, mealTime: String): MealEntity?

    // Sums up the cost of all meals for a user
    // Used to calculate the budget spent on Home screen
    // Returns null if no meals exist
    @Query("SELECT SUM(cost) FROM meals WHERE userEmail = :email")
    suspend fun getTotalSpent(email: String): Double?

    // Counts how many meals a user has
    // Returns 0 if no meals exist
    @Query("SELECT COUNT(*) FROM meals WHERE userEmail = :email")
    suspend fun getMealCount(email: String): Int

    // Deletes a meal by its ID
    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMeal(id: Int)
}
package com.example.mealmate.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MealDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Query("SELECT * FROM meals WHERE userEmail = :email")
    suspend fun getMealsByUser(email: String): List<MealEntity>

    @Query("SELECT * FROM meals WHERE userEmail = :email AND mealTime = :mealTime LIMIT 1")
    suspend fun getMealByTime(email: String, mealTime: String): MealEntity?

    @Query("SELECT SUM(cost) FROM meals WHERE userEmail = :email")
    suspend fun getTotalSpent(email: String): Double?

    @Query("SELECT COUNT(*) FROM meals WHERE userEmail = :email")
    suspend fun getMealCount(email: String): Int

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMeal(id: Int)
}
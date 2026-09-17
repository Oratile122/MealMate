package com.example.mealmate

import com.example.mealmate.database.MealEntity
import org.junit.Assert.*
import org.junit.Test

class MealEntityTest {

    @Test
    fun `test MealEntity with valid data`() {
        val meal = MealEntity(
            id = 1,
            userEmail = "test@email.com",
            name = "Oat Porridge",
            category = "Breakfast",
            ingredients = "Oats, Milk, Banana",
            cost = 28.0,
            calories = 340
        )

        assertEquals("Oat Porridge", meal.name)
        assertEquals("Breakfast", meal.category)
        assertEquals(28.0, meal.cost, 0.01)
        assertEquals(340, meal.calories)
        assertEquals("test@email.com", meal.userEmail)
    }

    @Test
    fun `test MealEntity default values`() {
        val meal = MealEntity(
            userEmail = "test@email.com",
            name = "Test",
            category = "Lunch",
            ingredients = "Test",
            cost = 10.0
        )

        assertEquals(0, meal.calories)
        assertEquals("", meal.day)
        assertEquals("", meal.mealTime)
    }
}
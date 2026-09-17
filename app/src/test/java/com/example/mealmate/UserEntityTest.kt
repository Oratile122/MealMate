package com.example.mealmate

import com.example.mealmate.database.UserEntity
import org.junit.Assert.*
import org.junit.Test

class UserEntityTest {

    @Test
    fun `test UserEntity with valid data`() {
        val user = UserEntity(
            id = 1,
            email = "test@email.com",
            displayName = "Test User",
            password = "password123",
            budget = 500.0,
            dietaryPreference = "None",
            language = "English",
            notifications = true,
            isLoggedIn = false
        )

        assertEquals(1, user.id)
        assertEquals("test@email.com", user.email)
        assertEquals("Test User", user.displayName)
        assertEquals(500.0, user.budget, 0.01)
        assertFalse(user.isLoggedIn)
    }

    @Test
    fun `test UserEntity copy changes isLoggedIn`() {
        val user = UserEntity(
            email = "test@email.com",
            displayName = "Test",
            password = "pass"
        )

        val loggedIn = user.copy(isLoggedIn = true)

        assertFalse(user.isLoggedIn)
        assertTrue(loggedIn.isLoggedIn)
    }
}
package com.example.mealmate

import com.example.mealmate.api.AuthResponse
import org.junit.Assert.*
import org.junit.Test

class AuthResponseTest {

    @Test
    fun `test AuthResponse with valid data`() {
        val response = AuthResponse(
            message = "Login successful",
            userId = 1,
            fullName = "Oratile Morudi",
            email = "oratile@email.com"
        )

        assertEquals("Login successful", response.message)
        assertEquals(1, response.userId)
        assertEquals("Oratile Morudi", response.fullName)
        assertEquals("oratile@email.com", response.email)
    }

    @Test
    fun `test AuthResponse with null values`() {
        val response = AuthResponse(message = "Failed")

        assertNull(response.userId)
        assertNull(response.fullName)
        assertNull(response.email)
    }
}
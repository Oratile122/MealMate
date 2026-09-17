package com.example.mealmate

import com.example.mealmate.api.RegisterRequest
import org.junit.Assert.*
import org.junit.Test

class RegisterRequestTest {

    @Test
    fun `test RegisterRequest with valid data`() {
        val request = RegisterRequest(
            fullName = "Test User",
            email = "test@email.com",
            password = "password123"
        )

        assertEquals("Test User", request.fullName)
        assertEquals("test@email.com", request.email)
        assertEquals("password123", request.password)
    }
}
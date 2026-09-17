package com.example.mealmate

import com.example.mealmate.api.LoginRequest
import org.junit.Assert.*
import org.junit.Test

class LoginRequestTest {

    @Test
    fun `test LoginRequest stores email and password`() {
        val request = LoginRequest("test@email.com", "password123")

        assertEquals("test@email.com", request.email)
        assertEquals("password123", request.password)
    }

    @Test
    fun `test LoginRequest equality`() {
        val r1 = LoginRequest("test@email.com", "pass")
        val r2 = LoginRequest("test@email.com", "pass")

        assertEquals(r1, r2)
    }
}
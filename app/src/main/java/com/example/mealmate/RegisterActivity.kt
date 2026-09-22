package com.example.mealmate

// RegisterActivity - handles new user registration
// Saves user to RoomDB first (offline), then tries REST API (online)

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mealmate.api.RegisterRequest
import com.example.mealmate.api.RetrofitClient
import com.example.mealmate.database.AppDatabase
import com.example.mealmate.database.UserEntity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    // Tag for Logcat logging
    private val TAG = "RegisterActivity"

    // Database reference
    private lateinit var db: AppDatabase

    // Called when screen is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Get database instance
        db = AppDatabase.getInstance(this)

        // Link UI variables to XML views
        val etFullName: TextInputEditText = findViewById(R.id.etFullName)
        val etEmail: TextInputEditText = findViewById(R.id.etRegisterEmail)
        val etPassword: TextInputEditText = findViewById(R.id.etRegisterPassword)
        val etConfirmPassword: TextInputEditText = findViewById(R.id.etConfirmPassword)
        val btnCreateAccount: Button = findViewById(R.id.btnCreateAccount)
        val tvLogin: TextView = findViewById(R.id.tvLogin)
        val tvBack: TextView = findViewById(R.id.tvBack)

        // Back and Login links just close this screen
        tvBack.setOnClickListener { finish() }
        tvLogin.setOnClickListener { finish() }

        // Register button clicked
        btnCreateAccount.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            // Validate all input fields
            when {
                fullName.isEmpty() -> {
                    etFullName.error = "Please enter your full name"
                    etFullName.requestFocus()
                }
                email.isEmpty() -> {
                    etEmail.error = "Please enter your email"
                    etEmail.requestFocus()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etEmail.error = "Invalid email address"
                    etEmail.requestFocus()
                }
                password.isEmpty() -> {
                    etPassword.error = "Please create a password"
                    etPassword.requestFocus()
                }
                password.length < 6 -> {
                    etPassword.error = "Password must be at least 6 characters"
                    etPassword.requestFocus()
                }
                password != confirmPassword -> {
                    etConfirmPassword.error = "Passwords do not match"
                    etConfirmPassword.requestFocus()
                }
                else -> {
                    // All valid - create the account
                    registerUser(fullName, email, password, btnCreateAccount)
                }
            }
        }
    }

    // Creates a new user account - saves to RoomDB and tries API
    private fun registerUser(fullName: String, email: String, password: String, button: Button) {
        button.isEnabled = false
        button.text = "Creating account..."

        lifecycleScope.launch {
            // Check if this email is already registered in RoomDB
            val exists = withContext(Dispatchers.IO) {
                db.userDao().userExists(email)
            }

            if (exists > 0) {
                // Email already used - show error
                Toast.makeText(this@RegisterActivity, "Email already registered!", Toast.LENGTH_LONG).show()
                button.isEnabled = true
                button.text = getString(R.string.register_button)
                return@launch
            }

            // Create user entity to store in RoomDB
            val user = UserEntity(
                email = email,
                displayName = fullName,
                password = password,
                budget = 500.0,
                dietaryPreference = "None",
                language = "English",
                notifications = true,
                isLoggedIn = false
            )

            // STEP 1: Save user to RoomDB (offline)
            withContext(Dispatchers.IO) {
                db.userDao().insertUser(user)
            }
            Log.d(TAG, "User saved to RoomDB: $email")

            // STEP 2: Try to register on REST API (online)
            // Even if the API fails, RoomDB has the user so registration succeeds
            try {
                val request = RegisterRequest(fullName, email, password)
                RetrofitClient.apiService.register(request).enqueue(
                    object : retrofit2.Callback<com.example.mealmate.api.AuthResponse> {
                        override fun onResponse(
                            call: retrofit2.Call<com.example.mealmate.api.AuthResponse>,
                            response: retrofit2.Response<com.example.mealmate.api.AuthResponse>
                        ) {
                            // Log API response and finish registration
                            Log.d(TAG, "API status: ${response.code()}")
                            finishRegistration(button)
                        }

                        override fun onFailure(
                            call: retrofit2.Call<com.example.mealmate.api.AuthResponse>,
                            t: Throwable
                        ) {
                            // API failed but RoomDB saved - still successful
                            Log.e(TAG, "API error: ${t.message}")
                            finishRegistration(button)
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "API exception: ${e.message}")
                finishRegistration(button)
            }
        }
    }

    // Shows success message and closes the screen
    private fun finishRegistration(button: Button) {
        button.isEnabled = true
        button.text = getString(R.string.register_button)
        Toast.makeText(
            this@RegisterActivity,
            "Registration successful!",
            Toast.LENGTH_SHORT
        ).show()
        finish()   // Return to Login screen
    }
}
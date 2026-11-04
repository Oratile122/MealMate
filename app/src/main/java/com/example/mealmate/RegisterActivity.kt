package com.example.mealmate

// RegisterActivity - handles new user registration
// Uses Firebase Authentication for email/password signup

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : BaseActivity() {

    private lateinit var authManager: FirebaseAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth manager
        authManager = FirebaseAuthManager(this)

        // Link UI views
        val etFullName: TextInputEditText = findViewById(R.id.etFullName)
        val etEmail: TextInputEditText = findViewById(R.id.etRegisterEmail)
        val etPassword: TextInputEditText = findViewById(R.id.etRegisterPassword)
        val etConfirmPassword: TextInputEditText = findViewById(R.id.etConfirmPassword)
        val btnCreateAccount: Button = findViewById(R.id.btnCreateAccount)
        val tvLogin: TextView = findViewById(R.id.tvLogin)
        val tvBack: TextView = findViewById(R.id.tvBack)

        // Back and Login links close this screen
        tvBack.setOnClickListener { finish() }
        tvLogin.setOnClickListener { finish() }

        btnCreateAccount.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            // Validate input
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
                else -> registerUser(fullName, email, password, btnCreateAccount)
            }
        }
    }

    // Registers the user with Firebase
    private fun registerUser(fullName: String, email: String, password: String, button: Button) {
        button.isEnabled = false
        button.text = "Creating account..."

        authManager.register(email, password, fullName) { success, error ->
            runOnUiThread {
                button.isEnabled = true
                button.text = getString(R.string.register_button)

                if (success) {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    finish()  // Return to Login
                } else {
                    Toast.makeText(this, error ?: "Registration failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
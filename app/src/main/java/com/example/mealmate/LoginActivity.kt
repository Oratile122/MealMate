package com.example.mealmate

// LoginActivity - handles user login
// Checks RoomDB first (offline), then falls back to REST API (online)

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mealmate.api.LoginRequest
import com.example.mealmate.api.RetrofitClient
import com.example.mealmate.database.AppDatabase
import com.example.mealmate.database.UserEntity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    // Tag for Logcat logging
    private val TAG = "LoginActivity"

    // Database and preferences references
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences

    // Called when screen is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Get database and preferences instances
        db = AppDatabase.getInstance(this)
        prefs = getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE)

        // Link UI variables to XML views
        val etEmail: TextInputEditText = findViewById(R.id.etEmail)
        val etPassword: TextInputEditText = findViewById(R.id.etPassword)
        val btnLogin: Button = findViewById(R.id.btnLogin)
        val btnGoogleSignIn: Button = findViewById(R.id.btnGoogleSignIn)
        val tvRegister: TextView = findViewById(R.id.tvRegister)

        // Login button clicked
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            // Validate input fields
            when {
                email.isEmpty() -> {
                    etEmail.error = "Please enter your email"
                    etEmail.requestFocus()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etEmail.error = "Invalid email address"
                    etEmail.requestFocus()
                }
                password.isEmpty() -> {
                    etPassword.error = "Please enter your password"
                    etPassword.requestFocus()
                }
                else -> {
                    // Input is valid, attempt login
                    loginUser(email, password, btnLogin)
                }
            }
        }

        // Google sign-in (not implemented yet)
        btnGoogleSignIn.setOnClickListener {
            Toast.makeText(this, "Google Sign-In coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Register link - open RegisterActivity
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // Handles user login - checks RoomDB first, then API
    private fun loginUser(email: String, password: String, button: Button) {
        button.isEnabled = false
        button.text = "Logging in..."

        lifecycleScope.launch {
            // STEP 1: Check RoomDB for user (offline login)
            val localUser = withContext(Dispatchers.IO) {
                db.userDao().getUserByEmail(email)
            }

            if (localUser != null) {
                // User exists in RoomDB - verify password
                if (localUser.password == password) {
                    // Mark this user as logged in
                    withContext(Dispatchers.IO) {
                        db.userDao().logoutAllUsers()
                        db.userDao().updateUser(localUser.copy(isLoggedIn = true))
                    }
                    Log.d(TAG, "User logged in via RoomDB: $email")

                    // Save session to SharedPreferences
                    prefs.edit()
                        .putString("email", localUser.email)
                        .putString("displayName", localUser.displayName)
                        .apply()
                    Log.d(TAG, "Saved to prefs: email='${localUser.email}', name='${localUser.displayName}'")

                    // Sync with API in background (doesn't block login)
                    trySyncWithApi(email, password)

                    Toast.makeText(
                        this@LoginActivity,
                        "Login successful!",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToHome(localUser.copy(isLoggedIn = true))
                } else {
                    // Wrong password
                    button.isEnabled = true
                    button.text = getString(R.string.login_button)
                    Toast.makeText(this@LoginActivity, "Incorrect password", Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            // STEP 2: Not in RoomDB - try REST API (online login)
            val request = LoginRequest(email, password)
            RetrofitClient.apiService.login(request).enqueue(
                object : retrofit2.Callback<com.example.mealmate.api.AuthResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<com.example.mealmate.api.AuthResponse>,
                        response: retrofit2.Response<com.example.mealmate.api.AuthResponse>
                    ) {
                        if (response.isSuccessful) {
                            // API login successful
                            val authResponse = response.body()
                            val displayName = authResponse?.fullName ?: email.substringBefore("@")

                            // Create user entity to save in RoomDB
                            val user = UserEntity(
                                email = email,
                                displayName = displayName,
                                password = password,
                                budget = 500.0,
                                dietaryPreference = "None",
                                language = "English",
                                notifications = true,
                                isLoggedIn = true
                            )

                            lifecycleScope.launch {
                                // Save user to RoomDB for future offline login
                                withContext(Dispatchers.IO) {
                                    db.userDao().logoutAllUsers()
                                    db.userDao().insertUser(user)
                                }

                                // Save session to SharedPreferences
                                prefs.edit()
                                    .putString("email", user.email)
                                    .putString("displayName", user.displayName)
                                    .apply()
                                Log.d(TAG, "Saved to prefs (API): email='${user.email}', name='${user.displayName}'")

                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login successful!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateToHome(user)
                            }
                        } else {
                            // API returned error (wrong credentials)
                            button.isEnabled = true
                            button.text = getString(R.string.login_button)
                            Toast.makeText(
                                this@LoginActivity,
                                "Incorrect email or password",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: retrofit2.Call<com.example.mealmate.api.AuthResponse>,
                        t: Throwable
                    ) {
                        // API unreachable (no internet or server down)
                        button.isEnabled = true
                        button.text = getString(R.string.login_button)
                        Log.e(TAG, "API error: ${t.message}")
                        Toast.makeText(
                            this@LoginActivity,
                            "No account found. Please register first.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
        }
    }

    // Silently syncs login with API (used after offline login succeeds)
    private fun trySyncWithApi(email: String, password: String) {
        try {
            val request = LoginRequest(email, password)
            RetrofitClient.apiService.login(request).enqueue(
                object : retrofit2.Callback<com.example.mealmate.api.AuthResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<com.example.mealmate.api.AuthResponse>,
                        response: retrofit2.Response<com.example.mealmate.api.AuthResponse>
                    ) {
                        Log.d(TAG, "Background sync status: ${response.code()}")
                    }

                    override fun onFailure(
                        call: retrofit2.Call<com.example.mealmate.api.AuthResponse>,
                        t: Throwable
                    ) {
                        // Offline mode is fine - just log it
                        Log.d(TAG, "Background sync failed (offline): ${t.message}")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync exception: ${e.message}")
        }
    }

    // Opens the Home screen with user data
    private fun navigateToHome(user: UserEntity) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("USER_EMAIL", user.email)
        intent.putExtra("USER_NAME", user.displayName)
        startActivity(intent)
        finish()   // Close login so back button doesn't return here
    }
}
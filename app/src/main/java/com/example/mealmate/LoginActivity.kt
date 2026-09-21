package com.example.mealmate

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

    private val TAG = "LoginActivity"
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = AppDatabase.getInstance(this)
        prefs = getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE)

        val etEmail: TextInputEditText = findViewById(R.id.etEmail)
        val etPassword: TextInputEditText = findViewById(R.id.etPassword)
        val btnLogin: Button = findViewById(R.id.btnLogin)
        val btnGoogleSignIn: Button = findViewById(R.id.btnGoogleSignIn)
        val tvRegister: TextView = findViewById(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

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
                    loginUser(email, password, btnLogin)
                }
            }
        }

        btnGoogleSignIn.setOnClickListener {
            Toast.makeText(this, "Google Sign-In coming soon!", Toast.LENGTH_SHORT).show()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String, button: Button) {
        button.isEnabled = false
        button.text = "Logging in..."

        lifecycleScope.launch {
            // Check RoomDB first (offline)
            val localUser = withContext(Dispatchers.IO) {
                db.userDao().getUserByEmail(email)
            }

            if (localUser != null) {
                if (localUser.password == password) {
                    withContext(Dispatchers.IO) {
                        db.userDao().logoutAllUsers()
                        db.userDao().updateUser(localUser.copy(isLoggedIn = true))
                    }
                    Log.d(TAG, "User logged in via RoomDB: $email")


                    prefs.edit()
                        .putString("email", localUser.email)
                        .putString("displayName", localUser.displayName)
                        .apply()
                    Log.d(TAG, "Saved to prefs: email='${localUser.email}', name='${localUser.displayName}'")

                    // Sync with API in background
                    trySyncWithApi(email, password)

                    Toast.makeText(
                        this@LoginActivity,
                        "Login successful!",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToHome(localUser.copy(isLoggedIn = true))
                } else {
                    button.isEnabled = true
                    button.text = getString(R.string.login_button)
                    Toast.makeText(this@LoginActivity, "Incorrect password", Toast.LENGTH_LONG).show()
                }
                return@launch
            }

            // Not in RoomDB — try REST API
            val request = LoginRequest(email, password)
            RetrofitClient.apiService.login(request).enqueue(
                object : retrofit2.Callback<com.example.mealmate.api.AuthResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<com.example.mealmate.api.AuthResponse>,
                        response: retrofit2.Response<com.example.mealmate.api.AuthResponse>
                    ) {
                        if (response.isSuccessful) {
                            val authResponse = response.body()
                            val displayName = authResponse?.fullName ?: email.substringBefore("@")

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
                                withContext(Dispatchers.IO) {
                                    db.userDao().logoutAllUsers()
                                    db.userDao().insertUser(user)
                                }

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
                        Log.d(TAG, "Background sync failed (offline): ${t.message}")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync exception: ${e.message}")
        }
    }

    private fun navigateToHome(user: UserEntity) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("USER_EMAIL", user.email)
        intent.putExtra("USER_NAME", user.displayName)
        startActivity(intent)
        finish()
    }
}
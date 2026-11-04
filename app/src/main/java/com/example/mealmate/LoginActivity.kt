package com.example.mealmate

// LoginActivity - handles user login
// Uses Firebase Authentication for email/password AND Google SSO

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : BaseActivity() {

    private val TAG = "LoginActivity"
    private lateinit var authManager: FirebaseAuthManager

    // Request code for Google Sign-In result
    private val RC_SIGN_IN = 9001
    // Request code for Notification Permission
    private val RC_NOTIFICATION_PERM = 101

    // UI views
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleSignIn: Button
    private lateinit var tvRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 🚨 ASK FOR NOTIFICATION PERMISSION (Android 13+)
        askNotificationPermission()

        // Initialize Firebase Auth manager
        authManager = FirebaseAuthManager(this)

        // If user is already logged in, skip to Home
        if (authManager.isLoggedIn()) {
            navigateToHome()
            return
        }

        // Link UI views
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        tvRegister = findViewById(R.id.tvRegister)

        // Login button clicked
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
                else -> loginWithEmail(email, password)
            }
        }

        // Google Sign-In button clicked
        btnGoogleSignIn.setOnClickListener {
            startGoogleSignIn()
        }

        // Register link clicked
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // Checks and requests notification permission for Android 13+
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    RC_NOTIFICATION_PERM
                )
            }
        }
    }

    // Logs in with Firebase email/password
    private fun loginWithEmail(email: String, password: String) {
        btnLogin.isEnabled = false
        btnLogin.text = "Logging in..."

        authManager.login(email, password) { success, error ->
            runOnUiThread {
                btnLogin.isEnabled = true
                btnLogin.text = getString(R.string.login_button)

                if (success) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                } else {
                    Toast.makeText(this, error ?: "Login failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Launches the Google Sign-In picker
    private fun startGoogleSignIn() {
        val signInIntent = authManager.getGoogleSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Handles the result from the Google Sign-In picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                if (idToken != null) {
                    authManager.signInWithGoogle(idToken) { success, error ->
                        runOnUiThread {
                            if (success) {
                                Toast.makeText(this, "Google sign-in successful!", Toast.LENGTH_SHORT).show()
                                navigateToHome()
                            } else {
                                Toast.makeText(this, error ?: "Google sign-in failed", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Google sign-in failed: No ID token", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign-in error: ${e.message}")
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Navigates to Home screen and closes Login
    private fun navigateToHome() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    val db = FirebaseFirestore.getInstance()

                    db.collection("users").document(currentUser.uid)
                        .update("fcmToken", token)
                        .addOnSuccessListener {
                            Log.d(TAG, "FCM Token successfully saved to Firestore: $token")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to save FCM Token", e)
                        }
                } else {
                    Log.e(TAG, "Fetching FCM registration token failed", task.exception)
                }
            }
        }

        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
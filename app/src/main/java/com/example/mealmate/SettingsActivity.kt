package com.example.mealmate

// SettingsActivity - shows user profile and preferences
// Uses Firebase Auth for user session and Firestore for saving preferences

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class SettingsActivity : BaseActivity() {

    private val TAG = "SettingsActivity"

    // Firebase instances
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Local preferences for fallback
    private lateinit var prefs: SharedPreferences

    // UI elements
    private lateinit var tvDisplayName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var spinnerDietary: Spinner
    private lateinit var spinnerLanguage: Spinner
    private lateinit var switchNotifications: Switch
    private lateinit var btnSave: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        prefs = getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE)

        // Link UI
        tvDisplayName = findViewById(R.id.tvDisplayName)
        tvEmail = findViewById(R.id.tvEmail)
        spinnerDietary = findViewById(R.id.spinnerDietary)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        switchNotifications = findViewById(R.id.switchNotifications)
        btnSave = findViewById(R.id.btnSave)
        btnLogout = findViewById(R.id.btnLogout)
        val btnBack = findViewById<TextView>(R.id.btnBack)

        // Setup spinners
        val dietaryOptions = arrayOf("None", "Vegetarian", "Vegan", "Gluten-Free")
        val dietaryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dietaryOptions)
        dietaryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDietary.adapter = dietaryAdapter

        val languageOptions = arrayOf("English", "IsiZulu", "Afrikaans")
        val languageAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageOptions)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = languageAdapter

        // Get current Firebase user
        val user = auth.currentUser
        if (user != null) {
            tvEmail.text = user.email ?: "No email"
            tvDisplayName.text = user.displayName ?: "Guest User"

            // Load preferences from Firestore
            loadUserPreferences(user.uid)
        } else {
            // No user - go back to login
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Back button
        btnBack.setOnClickListener { finish() }

        // Save button
        btnSave.setOnClickListener {
            saveUserPreferences()
        }

        // Logout button
        btnLogout.setOnClickListener {
            auth.signOut()
            prefs.edit().clear().apply()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // Load user preferences from Firestore
    private fun loadUserPreferences(userId: String) {
        lifecycleScope.launch {
            try {
                val document = withContext(Dispatchers.IO) {
                    db.collection("users").document(userId).get().await()
                }

                if (document.exists()) {
                    val dietary = document.getString("dietaryPreference") ?: "None"
                    val language = document.getString("language") ?: "English"
                    val notifications = document.getBoolean("notifications") ?: true

                    // Set spinners
                    val dietaryOptions = arrayOf("None", "Vegetarian", "Vegan", "Gluten-Free")
                    spinnerDietary.setSelection(dietaryOptions.indexOf(dietary).coerceAtLeast(0))

                    val languageOptions = arrayOf("English", "IsiZulu", "Afrikaans")
                    spinnerLanguage.setSelection(languageOptions.indexOf(language).coerceAtLeast(0))

                    switchNotifications.isChecked = notifications

                    Log.d(TAG, "Preferences loaded from Firestore")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading preferences: ${e.message}")
            }
        }
    }

    // Save user preferences to Firestore
    private fun saveUserPreferences() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        btnSave.isEnabled = false
        btnSave.text = "Saving..."

        val userId = user.uid
        val dietary = spinnerDietary.selectedItem.toString()
        val language = spinnerLanguage.selectedItem.toString()
        val notifications = switchNotifications.isChecked

        lifecycleScope.launch {
            try {
                val userData = hashMapOf(
                    "dietaryPreference" to dietary,
                    "language" to language,
                    "notifications" to notifications,
                    "email" to (user.email ?: ""),
                    "displayName" to (user.displayName ?: "")
                )

                withContext(Dispatchers.IO) {
                    db.collection("users").document(userId)
                        .set(userData)
                        .await()
                }

                Log.d(TAG, "Preferences saved to Firestore")

                runOnUiThread {
                    Toast.makeText(this@SettingsActivity, "Settings saved!", Toast.LENGTH_SHORT).show()
                    btnSave.isEnabled = true
                    btnSave.text = getString(R.string.settings_save)

                    // Apply the language change immediately
                    setLocale(language)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving preferences: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@SettingsActivity, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    btnSave.isEnabled = true
                    btnSave.text = getString(R.string.settings_save)
                }
            }
        }
    }

    // Changes the app language and restarts the app to apply it everywhere
    private fun setLocale(language: String) {
        val locale = when (language) {
            "IsiZulu" -> Locale("zu", "ZA")
            "Afrikaans" -> Locale("af", "ZA")
            else -> Locale("en", "ZA")
        }

        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)

        // Save the selected language code (en, zu, af) to SharedPreferences
        val langCode = locale.language
        prefs.edit().putString("app_language", langCode).apply()

        // Force the app to restart from the Home screen to apply the language everywhere
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
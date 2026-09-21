package com.example.mealmate

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mealmate.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private val TAG = "SettingsActivity"
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        db = AppDatabase.getInstance(this)
        prefs = getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE)

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val tvDisplayName = findViewById<TextView>(R.id.tvDisplayName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val spinnerDietary = findViewById<Spinner>(R.id.spinnerDietary)
        val spinnerLanguage = findViewById<Spinner>(R.id.spinnerLanguage)
        val switchNotifications = findViewById<Switch>(R.id.switchNotifications)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnLogout = findViewById<Button>(R.id.btnLogout)


        val userEmail = prefs.getString("email", "")
            ?: intent.getStringExtra("USER_EMAIL") ?: ""
        val userName = prefs.getString("displayName", "Guest User")
            ?: intent.getStringExtra("USER_NAME") ?: "Guest User"

        Log.d(TAG, "Settings loaded → email='$userEmail', name='$userName'")


        tvDisplayName.text = userName
        tvEmail.text = if (userEmail.isNotEmpty()) userEmail else "No email found"

        // Setup dietary spinner
        val dietaryOptions = arrayOf("None", "Vegetarian", "Vegan", "Gluten-Free")
        val dietaryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dietaryOptions)
        dietaryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDietary.adapter = dietaryAdapter

        // Setup language spinner
        val languageOptions = arrayOf("English", "IsiZulu", "Afrikaans")
        val languageAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageOptions)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = languageAdapter


        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                db.userDao().getUserByEmail(userEmail)
            }
            if (user != null) {
                Log.d(TAG, "User found in DB: ${user.displayName}, ${user.email}")

                // Show DB values (source of truth)
                tvDisplayName.text = user.displayName
                tvEmail.text = user.email

                // Set spinner to current values
                val dietaryPos = dietaryOptions.indexOf(user.dietaryPreference)
                if (dietaryPos >= 0) spinnerDietary.setSelection(dietaryPos)

                val langPos = languageOptions.indexOf(user.language)
                if (langPos >= 0) spinnerLanguage.setSelection(langPos)

                switchNotifications.isChecked = user.notifications
            } else {
                Log.w(TAG, "User NOT found in DB for '$userEmail'")
            }
        }

        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            if (userEmail.isEmpty()) {
                Toast.makeText(this, "No user session found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = withContext(Dispatchers.IO) {
                    db.userDao().getUserByEmail(userEmail)
                }

                if (user != null) {
                    val updated = user.copy(
                        dietaryPreference = spinnerDietary.selectedItem.toString(),
                        language = spinnerLanguage.selectedItem.toString(),
                        notifications = switchNotifications.isChecked
                    )
                    withContext(Dispatchers.IO) {
                        db.userDao().updateUser(updated)
                    }
                    Log.d(TAG, "Settings saved for '$userEmail'")
                    Toast.makeText(this@SettingsActivity, "Settings saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SettingsActivity, "Could not save settings", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnLogout.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.userDao().logoutAllUsers()
                }
                Log.d(TAG, "User logged out (data preserved)")

                Toast.makeText(this@SettingsActivity, "Logged out", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}
package com.example.mealmate

// CreateMealActivity - allows users to add a new meal to their plan
// Saves the meal to RoomDB with the logged-in user's email
// Meal then appears on Home and Weekly Plan screens

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mealmate.database.AppDatabase
import com.example.mealmate.database.MealEntity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateMealActivity : AppCompatActivity() {

    // Tag for Logcat logging
    private val TAG = "CreateMealActivity"

    // Database and preferences references
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences

    // Called when screen is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_meal)

        // Get database and preferences instances
        db = AppDatabase.getInstance(this)
        prefs = getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE)

        // Link UI variables to XML views
        val btnBack = findViewById<TextView>(R.id.btnBack)
        val etMealName = findViewById<TextInputEditText>(R.id.etMealName)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val etIngredients = findViewById<TextInputEditText>(R.id.etIngredients)
        val etCost = findViewById<TextInputEditText>(R.id.etCost)
        val etCalories = findViewById<TextInputEditText>(R.id.etCalories)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Load meal categories from strings.xml into the spinner
        val categories = resources.getStringArray(R.array.meal_categories)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Back button closes this screen
        btnBack.setOnClickListener { finish() }

        // Save button clicked
        btnSave.setOnClickListener {
            val name = etMealName.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val ingredients = etIngredients.text.toString().trim()
            val costStr = etCost.text.toString().trim()
            val caloriesStr = etCalories.text.toString().trim()

            // Validate input fields
            when {
                name.isEmpty() -> {
                    etMealName.error = "Please enter meal name"
                    etMealName.requestFocus()
                }
                category == "Select a category" -> {
                    Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                }
                ingredients.isEmpty() -> {
                    etIngredients.error = "Please enter ingredients"
                    etIngredients.requestFocus()
                }
                costStr.isEmpty() -> {
                    etCost.error = "Please enter cost"
                    etCost.requestFocus()
                }
                else -> {
                    // Input is valid - save the meal
                    val cost = costStr.toDoubleOrNull() ?: 0.0
                    val calories = caloriesStr.toIntOrNull() ?: 0
                    saveMeal(name, category, ingredients, cost, calories, btnSave)
                }
            }
        }
    }

    // Saves the meal to RoomDB
    private fun saveMeal(
        name: String,
        category: String,
        ingredients: String,
        cost: Double,
        calories: Int,
        button: Button
    ) {
        button.isEnabled = false
        button.text = "Saving..."

        // Get the current user's email
        val userEmail = prefs.getString("email", "") ?: ""

        // Log what we're about to save (helps with debugging)
        Log.d(TAG, "=== SAVING MEAL ===")
        Log.d(TAG, "Name: $name")
        Log.d(TAG, "Category: '$category'")
        Log.d(TAG, "Cost: $cost")
        Log.d(TAG, "Calories: $calories")
        Log.d(TAG, "Email: '$userEmail'")
        Log.d(TAG, "===================")

        lifecycleScope.launch {
            // Create the meal object
            val meal = MealEntity(
                userEmail = userEmail,
                name = name,
                category = category,
                ingredients = ingredients,
                cost = cost,
                calories = calories,
                mealTime = category   // mealTime is same as category
            )

            // Save to RoomDB (on background thread)
            withContext(Dispatchers.IO) {
                db.mealDao().insertMeal(meal)
            }

            // Log success
            Log.d(TAG, "Meal saved! ID=${meal.id}, Category='$category', Email='$userEmail'")

            // Reset button and show confirmation
            button.isEnabled = true
            button.text = getString(R.string.create_meal_save)
            Toast.makeText(this@CreateMealActivity, "Meal saved!", Toast.LENGTH_SHORT).show()

            // Close this screen and return to Weekly Plan
            finish()
        }
    }
}
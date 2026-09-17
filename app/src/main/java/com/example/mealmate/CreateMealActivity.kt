package com.example.mealmate

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

    private val TAG = "CreateMealActivity"
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_meal)

        db = AppDatabase.getInstance(this)
        prefs = getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE)

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val etMealName = findViewById<TextInputEditText>(R.id.etMealName)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val etIngredients = findViewById<TextInputEditText>(R.id.etIngredients)
        val etCost = findViewById<TextInputEditText>(R.id.etCost)
        val etCalories = findViewById<TextInputEditText>(R.id.etCalories)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val categories = resources.getStringArray(R.array.meal_categories)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val name = etMealName.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val ingredients = etIngredients.text.toString().trim()
            val costStr = etCost.text.toString().trim()
            val caloriesStr = etCalories.text.toString().trim()

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
                    val cost = costStr.toDoubleOrNull() ?: 0.0
                    val calories = caloriesStr.toIntOrNull() ?: 0
                    saveMeal(name, category, ingredients, cost, calories, btnSave)
                }
            }
        }
    }

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

        val userEmail = prefs.getString("email", "") ?: ""


        Log.d(TAG, "=== SAVING MEAL ===")
        Log.d(TAG, "Name: $name")
        Log.d(TAG, "Category: '$category'")
        Log.d(TAG, "Cost: $cost")
        Log.d(TAG, "Calories: $calories")
        Log.d(TAG, "Email: '$userEmail'")
        Log.d(TAG, "===================")

        lifecycleScope.launch {
            val meal = MealEntity(
                userEmail = userEmail,
                name = name,
                category = category,
                ingredients = ingredients,
                cost = cost,
                calories = calories,
                mealTime = category
            )

            withContext(Dispatchers.IO) {
                db.mealDao().insertMeal(meal)
            }


            Log.d(TAG, " Meal saved! ID=${meal.id}, Category='$category', Email='$userEmail'")

            button.isEnabled = true
            button.text = getString(R.string.create_meal_save)
            Toast.makeText(this@CreateMealActivity, "Meal saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
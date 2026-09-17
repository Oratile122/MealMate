package com.example.mealmate

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mealmate.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeeklyPlanActivity : AppCompatActivity() {

    private val TAG = "WeeklyPlanActivity"
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_plan)

        db = AppDatabase.getInstance(this)
        prefs = getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE)

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val btnAddMeal = findViewById<Button>(R.id.btnAddMeal)

        btnBack.setOnClickListener { finish() }

        btnAddMeal.setOnClickListener {
            startActivity(Intent(this, CreateMealActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (::db.isInitialized) {
            loadSummary()
        }
    }

    private fun loadSummary() {
        val userEmail = prefs.getString("email", "") ?: ""

        lifecycleScope.launch {
            try {
                val meals = withContext(Dispatchers.IO) {
                    db.mealDao().getMealsByUser(userEmail)
                }

                Log.d(TAG, "=== WEEKLY PLAN DEBUG ===")
                Log.d(TAG, "User: '$userEmail'")
                Log.d(TAG, "Meals found: ${meals.size}")
                meals.forEach { meal ->
                    Log.d(TAG, "  → ${meal.name} (${meal.category}) - R${meal.cost}")
                }
                Log.d(TAG, "=========================")

                val totalCost = meals.sumOf { it.cost }
                val totalMeals = meals.size
                val totalSlots = 15
                val perDay = if (totalMeals > 0) totalCost / 5 else 0.0

                // Update summary TextViews
                val tvTotalCost = findViewById<TextView>(R.id.tvTotalCost)
                val tvMealsPlanned = findViewById<TextView>(R.id.tvMealsPlanned)
                val tvPerDayAvg = findViewById<TextView>(R.id.tvPerDayAvg)

                tvTotalCost.text = "R %.2f".format(totalCost)
                tvMealsPlanned.text = "$totalMeals/$totalSlots"
                tvPerDayAvg.text = "R %.2f".format(perDay)


                val breakfast = meals.find { it.category == "Breakfast" }
                val lunch = meals.find { it.category == "Lunch" }
                val dinner = meals.find { it.category == "Dinner" }

                // Optional: Show meal count toast so user knows it worked
                if (totalMeals > 0) {
                    Log.d(TAG, "Summary updated: $totalMeals meals, R$totalCost total")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading meals: ${e.message}", e)
            }
        }
    }
}
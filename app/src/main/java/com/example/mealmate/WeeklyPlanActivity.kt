package com.example.mealmate

// WeeklyPlanActivity - shows the weekly meal plan screen
// Displays current week number and dates, plus a summary of planned meals

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mealmate.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeeklyPlanActivity : AppCompatActivity() {

    // Tag for Logcat logging
    private val TAG = "WeeklyPlanActivity"

    // Database and preferences references
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences

    // Called when screen is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_plan)

        // Get database and preferences instances
        db = AppDatabase.getInstance(this)
        prefs = getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE)

        // Back and Add Meal buttons
        val btnBack = findViewById<TextView>(R.id.btnBack)
        val btnAddMeal = findViewById<Button>(R.id.btnAddMeal)

        // Back button closes this screen
        btnBack.setOnClickListener { finish() }

        // Add Meal button opens CreateMealActivity
        btnAddMeal.setOnClickListener {
            startActivity(Intent(this, CreateMealActivity::class.java))
        }

        // Show the current week and dates
        setCurrentWeekInfo()
    }

    // Refresh summary when returning to this screen
    override fun onResume() {
        super.onResume()
        if (::db.isInitialized) {
            loadSummary()
        }
    }

    // Sets the week label and date range based on today's date
    // Example: "Week 39 — September" and "Sep 22 – Sep 26, 2026"
    private fun setCurrentWeekInfo() {
        val tvWeekLabel = findViewById<TextView>(R.id.tvWeekLabel)
        val tvWeekDates = findViewById<TextView>(R.id.tvWeekDates)

        // Get today's date
        val calendar = Calendar.getInstance()
        val today = calendar.time

        // Get week number of the year (1-52)
        val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)

        // Get current month name (e.g. "September")
        val monthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
        val monthName = monthFormat.format(today)

        // Get this week's Monday
        val mondayCal = calendar.clone() as Calendar
        mondayCal.firstDayOfWeek = Calendar.MONDAY
        mondayCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        // Get this week's Friday
        val fridayCal = calendar.clone() as Calendar
        fridayCal.firstDayOfWeek = Calendar.MONDAY
        fridayCal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)

        // Format dates
        val dateFormat = SimpleDateFormat("MMM d", Locale.ENGLISH)
        val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)

        val mondayText = dateFormat.format(mondayCal.time)
        val fridayText = dateFormat.format(fridayCal.time)
        val yearText = yearFormat.format(today)

        // Update week label: "Week 39 — September"
        tvWeekLabel.text = "Week $weekOfYear — $monthName"

        // Update date range: "Sep 22 – Sep 26, 2026"
        tvWeekDates.text = "$mondayText – $fridayText, $yearText"

        // Log for debugging
        Log.d(TAG, "Week: $weekOfYear, Month: $monthName")
        Log.d(TAG, "Dates: $mondayText – $fridayText, $yearText")
    }

    // Loads meals from RoomDB and updates the summary section
    private fun loadSummary() {
        val userEmail = prefs.getString("email", "") ?: ""

        lifecycleScope.launch {
            try {
                // Get all meals for the logged-in user
                val meals = withContext(Dispatchers.IO) {
                    db.mealDao().getMealsByUser(userEmail)
                }

                // Log meals for debugging
                Log.d(TAG, "=== WEEKLY PLAN DEBUG ===")
                Log.d(TAG, "User: '$userEmail'")
                Log.d(TAG, "Meals found: ${meals.size}")
                meals.forEach { meal ->
                    Log.d(TAG, "  → ${meal.name} (${meal.category}) - R${meal.cost}")
                }
                Log.d(TAG, "=========================")

                // Calculate summary values
                val totalCost = meals.sumOf { it.cost }
                val totalMeals = meals.size
                val totalSlots = 15   // 5 days × 3 meals = 15 total slots
                val perDay = if (totalMeals > 0) totalCost / 5 else 0.0

                // Update summary TextViews
                val tvTotalCost = findViewById<TextView>(R.id.tvTotalCost)
                val tvMealsPlanned = findViewById<TextView>(R.id.tvMealsPlanned)
                val tvPerDayAvg = findViewById<TextView>(R.id.tvPerDayAvg)

                tvTotalCost.text = "R %.2f".format(totalCost)
                tvMealsPlanned.text = "$totalMeals/$totalSlots"
                tvPerDayAvg.text = "R %.2f".format(perDay)

            } catch (e: Exception) {
                // Log error instead of crashing
                Log.e(TAG, "Error loading meals: ${e.message}", e)
            }
        }
    }
}
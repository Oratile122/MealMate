package com.example.mealmate

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


        setCurrentWeekInfo()
    }

    override fun onResume() {
        super.onResume()
        if (::db.isInitialized) {
            loadSummary()
        }
    }


    private fun setCurrentWeekInfo() {
        val tvWeekLabel = findViewById<TextView>(R.id.tvWeekLabel)
        val tvWeekDates = findViewById<TextView>(R.id.tvWeekDates)

        // Get today's date
        val calendar = Calendar.getInstance()
        val today = calendar.time

        // Calculate week number (which week of the year)
        val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)

        // Get current month name
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

        // Format the date range
        val dateFormat = SimpleDateFormat("MMM d", Locale.ENGLISH)
        val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)

        val mondayText = dateFormat.format(mondayCal.time)
        val fridayText = dateFormat.format(fridayCal.time)
        val yearText = yearFormat.format(today)


        tvWeekLabel.text = "Week $weekOfYear — $monthName"


        tvWeekDates.text = "$mondayText – $fridayText, $yearText"

        Log.d(TAG, "Week: $weekOfYear, Month: $monthName")
        Log.d(TAG, "Dates: $mondayText – $fridayText, $yearText")
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

            } catch (e: Exception) {
                Log.e(TAG, "Error loading meals: ${e.message}", e)
            }
        }
    }
}
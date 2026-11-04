package com.example.mealmate

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.mealmate.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeeklyPlanActivity : BaseActivity() {

    private val TAG = "WeeklyPlanActivity"
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🚨 FORCE LANGUAGE UPDATE ON THIS SCREEN 🚨
        prefs = getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE)
        val langCode = prefs.getString("app_language", "en") ?: "en"
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        setContentView(R.layout.activity_weekly_plan)

        db = AppDatabase.getInstance(this)

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val btnAddMeal = findViewById<Button>(R.id.btnAddMeal)

        btnBack.setOnClickListener { finish() }
        btnAddMeal.setOnClickListener {
            startActivity(Intent(this, CreateMealActivity::class.java))
        }

        setupCurrentWeek()
    }

    override fun onResume() {
        super.onResume()
        if (::db.isInitialized) {
            loadMealsForGrid()
        }
    }

    private fun setupCurrentWeek() {
        val calendar = Calendar.getInstance()

        // Set to Monday of this week
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dateFormat = SimpleDateFormat("MMM d", Locale.ENGLISH)

        // Calculate dates for Mon-Fri
        val monday = calendar.time
        calendar.add(Calendar.DAY_OF_WEEK, 1)
        val tuesday = calendar.time
        calendar.add(Calendar.DAY_OF_WEEK, 1)
        val wednesday = calendar.time
        calendar.add(Calendar.DAY_OF_WEEK, 1)
        val thursday = calendar.time
        calendar.add(Calendar.DAY_OF_WEEK, 1)
        val friday = calendar.time

        // Update header texts
        findViewById<TextView>(R.id.tvHeaderMon).text = "MON\n${dateFormat.format(monday).split(" ")[1]}"
        findViewById<TextView>(R.id.tvHeaderTue).text = "TUE\n${dateFormat.format(tuesday).split(" ")[1]}"
        findViewById<TextView>(R.id.tvHeaderWed).text = "WED\n${dateFormat.format(wednesday).split(" ")[1]}"
        findViewById<TextView>(R.id.tvHeaderThu).text = "THU\n${dateFormat.format(thursday).split(" ")[1]}"
        findViewById<TextView>(R.id.tvHeaderFri).text = "FRI\n${dateFormat.format(friday).split(" ")[1]}"

        // Update week label and date range
        val weekNumber = calendar.get(Calendar.WEEK_OF_YEAR)
        val monthName = SimpleDateFormat("MMMM", Locale.ENGLISH).format(monday)

        // Update the Week Label (using the string resource now!)
        findViewById<TextView>(R.id.tvWeekLabel).text = getString(R.string.weekly_week) + " $weekNumber — $monthName"
        findViewById<TextView>(R.id.tvWeekDates).text =
            "${dateFormat.format(monday)} – ${dateFormat.format(friday)}, ${calendar.get(Calendar.YEAR)}"
    }

    private fun loadMealsForGrid() {
        val userEmail = prefs.getString("email", "") ?: ""

        lifecycleScope.launch {
            // Fetch all meals for this user
            val meals = withContext(Dispatchers.IO) {
                db.mealDao().getMealsByUser(userEmail)
            }

            // Group by category
            val breakfast = meals.find { it.category == "Breakfast" }
            val lunch = meals.find { it.category == "Lunch" }
            val dinner = meals.find { it.category == "Dinner" }

            // Helper to set text or "+"
            fun setMealText(id: Int, mealName: String?) {
                val tv = findViewById<TextView>(id)
                if (mealName != null) {
                    tv.text = mealName
                    tv.setTextColor(Color.parseColor("#333333"))
                } else {
                    tv.text = "+"
                    tv.setTextColor(Color.parseColor("#19B957"))
                }
            }

            setMealText(R.id.tvMonBreakfast, breakfast?.name)
            setMealText(R.id.tvMonLunch, lunch?.name)
            setMealText(R.id.tvMonDinner, dinner?.name)

            val emptyCells = listOf(
                R.id.tvTueBreakfast, R.id.tvWedBreakfast, R.id.tvThuBreakfast, R.id.tvFriBreakfast,
                R.id.tvTueLunch, R.id.tvWedLunch, R.id.tvThuLunch, R.id.tvFriLunch,
                R.id.tvTueDinner, R.id.tvWedDinner, R.id.tvThuDinner, R.id.tvFriDinner
            )
            emptyCells.forEach { setMealText(it, null) }

            // Update summary
            val totalCost = meals.sumOf { it.cost }
            val totalMeals = meals.size
            val perDay = if (totalMeals > 0) totalCost / 5 else 0.0

            findViewById<TextView>(R.id.tvTotalCost).text = "R %.2f".format(totalCost)
            findViewById<TextView>(R.id.tvMealsPlanned).text = "$totalMeals/15"
            findViewById<TextView>(R.id.tvPerDayAvg).text = "R %.2f".format(perDay)

            Log.d(TAG, "Loaded ${meals.size} meals")
        }
    }
}
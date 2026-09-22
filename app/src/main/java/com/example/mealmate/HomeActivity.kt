package com.example.mealmate

// HomeActivity - main dashboard screen showing user info, budget, and today's meals
// Reads user and meal data from RoomDB, uses SharedPreferences for session

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mealmate.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    // Tag for Logcat logging
    private val TAG = "HomeActivity"

    // Database and preferences references
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences

    // UI views
    private lateinit var tvDate: TextView
    private lateinit var tvWelcome: TextView
    private lateinit var tvBudget: TextView
    private lateinit var tvBudgetSpent: TextView
    private lateinit var tvBudgetLeft: TextView
    private lateinit var tvBreakfastName: TextView
    private lateinit var tvBreakfastCost: TextView
    private lateinit var tvBreakfastCal: TextView
    private lateinit var tvLunchName: TextView
    private lateinit var tvLunchCost: TextView
    private lateinit var tvLunchCal: TextView
    private lateinit var tvDinnerName: TextView
    private lateinit var tvDinnerCost: TextView
    private lateinit var tvDinnerCal: TextView
    private lateinit var tvNutritionCal: TextView
    private lateinit var tvNutritionProtein: TextView
    private lateinit var tvNutritionCarbs: TextView

    // Current user session data
    private var userEmail: String = ""
    private var userName: String = ""

    // Called when screen is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Get database and preferences instances
        db = AppDatabase.getInstance(this)
        prefs = getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE)

        // Link UI variables to XML views
        tvDate = findViewById(R.id.tvDate)
        tvWelcome = findViewById(R.id.tvWelcome)
        tvBudget = findViewById(R.id.tvBudgetRemaining)
        tvBudgetSpent = findViewById(R.id.tvBudgetSpent)
        tvBudgetLeft = findViewById(R.id.tvBudgetLeft)
        tvBreakfastName = findViewById(R.id.tvBreakfastName)
        tvBreakfastCost = findViewById(R.id.tvBreakfastCost)
        tvBreakfastCal = findViewById(R.id.tvBreakfastCal)
        tvLunchName = findViewById(R.id.tvLunchName)
        tvLunchCost = findViewById(R.id.tvLunchCost)
        tvLunchCal = findViewById(R.id.tvLunchCal)
        tvDinnerName = findViewById(R.id.tvDinnerName)
        tvDinnerCost = findViewById(R.id.tvDinnerCost)
        tvDinnerCal = findViewById(R.id.tvDinnerCal)
        tvNutritionCal = findViewById(R.id.tvNutritionCal)
        tvNutritionProtein = findViewById(R.id.tvNutritionProtein)
        tvNutritionCarbs = findViewById(R.id.tvNutritionCarbs)

        // Show today's date (e.g. "Monday, 22 Sep")
        val currentDate = SimpleDateFormat("EEEE, d MMM", Locale.ENGLISH).format(Date())
        tvDate.text = currentDate
        Log.d(TAG, "Date set to: $currentDate")

        // Bottom navigation buttons
        val btnHome = findViewById<TextView>(R.id.btnHome)
        val btnPlan = findViewById<TextView>(R.id.btnPlan)
        val btnShopping = findViewById<TextView>(R.id.btnShopping)
        val btnProfile = findViewById<TextView>(R.id.btnProfile)

        // Get user session from SharedPreferences (main source)
        userEmail = prefs.getString("email", "") ?: ""
        userName = prefs.getString("displayName", "User") ?: "User"

        // If intent has newer data, use that instead
        intent.getStringExtra("USER_EMAIL")?.let { if (it.isNotEmpty()) userEmail = it }
        intent.getStringExtra("USER_NAME")?.let { if (it.isNotEmpty()) userName = it }

        Log.d(TAG, "onCreate: userEmail='$userEmail', userName='$userName'")

        // Show greeting
        tvWelcome.text = "Hi, $userName 🐟"

        // Bottom nav button actions
        btnHome.setOnClickListener {
            Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show()
        }

        btnPlan.setOnClickListener {
            startActivity(Intent(this, WeeklyPlanActivity::class.java))
        }

        btnShopping.setOnClickListener {
            startActivity(Intent(this, ShoppingListActivity::class.java))
        }

        btnProfile.setOnClickListener {
            // Open settings screen with user data
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("USER_EMAIL", userEmail)
            intent.putExtra("USER_NAME", userName)
            startActivity(intent)
        }

        // Load user and meal data
        loadUserData()
    }

    // Refresh data when returning to this screen
    override fun onResume() {
        super.onResume()
        if (::db.isInitialized && ::tvWelcome.isInitialized) {
            loadUserData()
        }
    }

    // Load user profile and meals from RoomDB
    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                // Re-read email from prefs in case it changed
                userEmail = prefs.getString("email", "") ?: userEmail
                Log.d(TAG, "loadUserData: looking for user '$userEmail'")

                // Get user from database
                val user = withContext(Dispatchers.IO) {
                    db.userDao().getUserByEmail(userEmail)
                }

                if (user != null) {
                    // Update greeting with real name
                    tvWelcome.text = "Hi, ${user.displayName} "

                    // Calculate budget
                    val totalBudget = user.budget
                    val spent = withContext(Dispatchers.IO) {
                        db.mealDao().getTotalSpent(userEmail) ?: 0.0
                    }
                    val left = totalBudget - spent
                    val percentSpent = if (totalBudget > 0) (spent / totalBudget * 100).toInt() else 0

                    // Update budget card
                    tvBudget.text = "R %.0f / R %.0f".format(spent, totalBudget)
                    tvBudgetSpent.text = "Spent $percentSpent%"
                    tvBudgetLeft.text = "R %.0f left".format(left)
                } else {
                    // User not found, show defaults
                    Log.w(TAG, "User not found in RoomDB for '$userEmail'")
                    tvBudget.text = "R 0 / R 500"
                    tvBudgetSpent.text = "Spent 0%"
                    tvBudgetLeft.text = "R 500 left"
                }

                // Get meals for this user
                val meals = withContext(Dispatchers.IO) {
                    db.mealDao().getMealsByUser(userEmail)
                }

                // Log meals for debugging
                Log.d(TAG, "=== HOME DEBUG ===")
                Log.d(TAG, "Looking for user: '$userEmail'")
                Log.d(TAG, "Found ${meals.size} meals")
                meals.forEach { meal ->
                    Log.d(TAG, "  → ID=${meal.id}, name='${meal.name}', category='${meal.category}', email='${meal.userEmail}'")
                }
                Log.d(TAG, "===================")

                // Find meals by category
                val breakfast = meals.find { it.category == "Breakfast" }
                val lunch = meals.find { it.category == "Lunch" }
                val dinner = meals.find { it.category == "Dinner" }

                // Show breakfast (or "Nothing planned")
                if (breakfast != null) {
                    tvBreakfastName.text = breakfast.name
                    tvBreakfastCost.text = "R %.0f".format(breakfast.cost)
                    tvBreakfastCal.text = "${breakfast.calories} kcal"
                } else {
                    tvBreakfastName.text = "Nothing planned"
                    tvBreakfastCost.text = "R 0"
                    tvBreakfastCal.text = "0 kcal"
                }

                // Show lunch (or "Nothing planned")
                if (lunch != null) {
                    tvLunchName.text = lunch.name
                    tvLunchCost.text = "R %.0f".format(lunch.cost)
                    tvLunchCal.text = "${lunch.calories} kcal"
                } else {
                    tvLunchName.text = "Nothing planned"
                    tvLunchCost.text = "R 0"
                    tvLunchCal.text = "0 kcal"
                }

                // Show dinner (or "Nothing planned")
                if (dinner != null) {
                    tvDinnerName.text = dinner.name
                    tvDinnerCost.text = "R %.0f".format(dinner.cost)
                    tvDinnerCal.text = "${dinner.calories} kcal"
                } else {
                    tvDinnerName.text = "Nothing planned"
                    tvDinnerCost.text = "R 0"
                    tvDinnerCal.text = "0 kcal"
                }

                // Nutrition totals (protein and carbs are estimates)
                val totalCalories = meals.sumOf { it.calories }
                val totalProtein = meals.size * 15   // ~15g protein per meal
                val totalCarbs = meals.size * 30     // ~30g carbs per meal

                tvNutritionCal.text = totalCalories.toString()
                tvNutritionProtein.text = "${totalProtein}g"
                tvNutritionCarbs.text = "${totalCarbs}g"

            } catch (e: Exception) {
                // Log error instead of crashing
                Log.e(TAG, "Error loading user data: ${e.message}", e)
            }
        }
    }
}
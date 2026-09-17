package com.example.mealmate

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

class HomeActivity : AppCompatActivity() {

    private val TAG = "HomeActivity"
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences

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

    private var userEmail: String = ""
    private var userName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        db = AppDatabase.getInstance(this)
        prefs = getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE)

        // Initialize views
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

        // Bottom navigation buttons
        val btnHome = findViewById<TextView>(R.id.btnHome)
        val btnPlan = findViewById<TextView>(R.id.btnPlan)
        val btnShopping = findViewById<TextView>(R.id.btnShopping)
        val btnProfile = findViewById<TextView>(R.id.btnProfile)

        // Get user data — prefer SharedPreferences (most reliable)
        userEmail = prefs.getString("email", "") ?: ""
        userName = prefs.getString("displayName", "User") ?: "User"

        // If intent has data, use that instead (more recent)
        intent.getStringExtra("USER_EMAIL")?.let { if (it.isNotEmpty()) userEmail = it }
        intent.getStringExtra("USER_NAME")?.let { if (it.isNotEmpty()) userName = it }

        Log.d(TAG, "onCreate: userEmail='$userEmail', userName='$userName'")

        tvWelcome.text = "Hi, $userName 🐟"

        // Bottom navigation click listeners
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
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("USER_EMAIL", userEmail)
            intent.putExtra("USER_NAME", userName)
            startActivity(intent)
        }

        // Load data
        loadUserData()
    }

    override fun onResume() {
        super.onResume()
        if (::db.isInitialized && ::tvWelcome.isInitialized) {
            loadUserData()
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                // Re-read user email from prefs (in case it changed)
                userEmail = prefs.getString("email", "") ?: userEmail
                Log.d(TAG, "loadUserData: looking for user '$userEmail'")

                val user = withContext(Dispatchers.IO) {
                    db.userDao().getUserByEmail(userEmail)
                }

                if (user != null) {
                    tvWelcome.text = "Hi, ${user.displayName} 🐟"

                    val totalBudget = user.budget
                    val spent = withContext(Dispatchers.IO) {
                        db.mealDao().getTotalSpent(userEmail) ?: 0.0
                    }
                    val left = totalBudget - spent
                    val percentSpent = if (totalBudget > 0) (spent / totalBudget * 100).toInt() else 0

                    tvBudget.text = "R %.0f / R %.0f".format(spent, totalBudget)
                    tvBudgetSpent.text = "Spent $percentSpent%"
                    tvBudgetLeft.text = "R %.0f left".format(left)
                } else {
                    Log.w(TAG, "User not found in RoomDB for '$userEmail'")
                    tvBudget.text = "R 0 / R 500"
                    tvBudgetSpent.text = "Spent 0%"
                    tvBudgetLeft.text = "R 500 left"
                }

                // ✅ Load meals from RoomDB
                val meals = withContext(Dispatchers.IO) {
                    db.mealDao().getMealsByUser(userEmail)
                }

                // ✅ DEBUG: Log every meal found
                Log.d(TAG, "=== HOME DEBUG ===")
                Log.d(TAG, "Looking for user: '$userEmail'")
                Log.d(TAG, "Found ${meals.size} meals")
                meals.forEach { meal ->
                    Log.d(TAG, "  → ID=${meal.id}, name='${meal.name}', category='${meal.category}', email='${meal.userEmail}'")
                }
                Log.d(TAG, "===================")

                val breakfast = meals.find { it.category == "Breakfast" }
                val lunch = meals.find { it.category == "Lunch" }
                val dinner = meals.find { it.category == "Dinner" }

                // Breakfast
                if (breakfast != null) {
                    tvBreakfastName.text = breakfast.name
                    tvBreakfastCost.text = "R %.0f".format(breakfast.cost)
                    tvBreakfastCal.text = "${breakfast.calories} kcal"
                } else {
                    tvBreakfastName.text = "Nothing planned"
                    tvBreakfastCost.text = "R 0"
                    tvBreakfastCal.text = "0 kcal"
                }

                // Lunch
                if (lunch != null) {
                    tvLunchName.text = lunch.name
                    tvLunchCost.text = "R %.0f".format(lunch.cost)
                    tvLunchCal.text = "${lunch.calories} kcal"
                } else {
                    tvLunchName.text = "Nothing planned"
                    tvLunchCost.text = "R 0"
                    tvLunchCal.text = "0 kcal"
                }

                // Dinner
                if (dinner != null) {
                    tvDinnerName.text = dinner.name
                    tvDinnerCost.text = "R %.0f".format(dinner.cost)
                    tvDinnerCal.text = "${dinner.calories} kcal"
                } else {
                    tvDinnerName.text = "Nothing planned"
                    tvDinnerCost.text = "R 0"
                    tvDinnerCal.text = "0 kcal"
                }

                // Nutrition summary
                val totalCalories = meals.sumOf { it.calories }
                val totalProtein = meals.size * 15
                val totalCarbs = meals.size * 30

                tvNutritionCal.text = totalCalories.toString()
                tvNutritionProtein.text = "${totalProtein}g"
                tvNutritionCarbs.text = "${totalCarbs}g"

            } catch (e: Exception) {
                Log.e(TAG, "Error loading user data: ${e.message}", e)
            }
        }
    }
}
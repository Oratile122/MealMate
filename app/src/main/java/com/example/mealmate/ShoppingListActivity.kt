package com.example.mealmate

// ShoppingListActivity - lets users check off shopping items
// Saves checked states to RoomDB so they persist across app restarts

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mealmate.database.AppDatabase
import com.example.mealmate.database.ShoppingItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShoppingListActivity : AppCompatActivity() {

    // Tag for Logcat logging
    private val TAG = "ShoppingListActivity"

    // Database and preferences references
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences

    // Summary text views
    private lateinit var tvItemsPicked: TextView
    private lateinit var tvTotal: TextView

    // Maps each checkbox to its item name and price
    private val checkBoxes: MutableMap<CheckBox, Pair<String, Double>> = mutableMapOf()

    // Called when screen is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        // Get database and preferences instances
        db = AppDatabase.getInstance(this)
        prefs = getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE)

        // Get current user's email for scoping their shopping list
        val userEmail = prefs.getString("email", "") ?: ""

        // Link UI variables to XML views
        val btnBack = findViewById<TextView>(R.id.btnBack)
        val btnClearList = findViewById<Button>(R.id.btnClearList)
        tvItemsPicked = findViewById(R.id.tvItemsPicked)
        tvTotal = findViewById(R.id.tvTotal)

        // Map each checkbox to its item name and price
        checkBoxes[findViewById(R.id.cbOats)] = "Oats" to 2.49
        checkBoxes[findViewById(R.id.cbMilk)] = "Milk" to 1.89
        checkBoxes[findViewById(R.id.cbBananas)] = "Bananas" to 0.99
        checkBoxes[findViewById(R.id.cbChicken)] = "Chicken" to 6.49
        checkBoxes[findViewById(R.id.cbWrap)] = "Wrap" to 2.29
        checkBoxes[findViewById(R.id.cbPasta)] = "Pasta" to 1.59

        // Load previously saved checked states from RoomDB
        lifecycleScope.launch {
            for ((checkBox, itemData) in checkBoxes) {
                val itemName = itemData.first
                val isChecked = withContext(Dispatchers.IO) {
                    db.shoppingItemDao().isItemChecked(userEmail, itemName)
                }
                checkBox.isChecked = isChecked ?: false
            }
            // Update the summary after loading all states
            updateSummary()
        }

        // Set checkbox listeners - saves state to RoomDB when toggled
        for ((checkBox, itemData) in checkBoxes) {
            val itemName = itemData.first
            val price = itemData.second

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val existing = db.shoppingItemDao().isItemChecked(userEmail, itemName)
                        if (existing == null) {
                            // First time checking this item - insert new record
                            db.shoppingItemDao().insertItem(
                                ShoppingItemEntity(
                                    itemName = itemName,
                                    userEmail = userEmail,
                                    isChecked = isChecked,
                                    price = price
                                )
                            )
                        } else {
                            // Item exists - just update the checked state
                            db.shoppingItemDao().updateChecked(userEmail, itemName, isChecked)
                        }
                    }
                    Log.d(TAG, "$itemName -> $isChecked")
                    updateSummary()
                }
            }
        }

        // Back button closes this screen
        btnBack.setOnClickListener { finish() }

        // Clear List button - unchecks everything and clears RoomDB records
        btnClearList.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.shoppingItemDao().uncheckAll(userEmail)
                }
                // Uncheck all checkboxes in the UI
                for ((cb, _) in checkBoxes) {
                    cb.isChecked = false
                }
                updateSummary()
                Toast.makeText(this@ShoppingListActivity, "List cleared!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Counts checked items and calculates total price, updates the summary views
    private fun updateSummary() {
        var pickedCount = 0
        var totalPrice = 0.0

        // Loop through all checkboxes and count checked ones
        for ((checkBox, itemData) in checkBoxes) {
            if (checkBox.isChecked) {
                pickedCount++
                totalPrice += itemData.second
            }
        }

        // Update the display
        tvItemsPicked.text = "$pickedCount of ${checkBoxes.size} items picked"
        tvTotal.text = "R %.2f".format(totalPrice)
    }
}
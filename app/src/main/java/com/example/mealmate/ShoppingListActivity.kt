package com.example.mealmate

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

    private val TAG = "ShoppingListActivity"
    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences

    private lateinit var tvItemsPicked: TextView
    private lateinit var tvTotal: TextView


    private val checkBoxes: MutableMap<CheckBox, Pair<String, Double>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        db = AppDatabase.getInstance(this)
        prefs = getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE)
        val userEmail = prefs.getString("email", "") ?: ""

        val btnBack = findViewById<TextView>(R.id.btnBack)
        val btnClearList = findViewById<Button>(R.id.btnClearList)
        tvItemsPicked = findViewById(R.id.tvItemsPicked)
        tvTotal = findViewById(R.id.tvTotal)


        checkBoxes[findViewById(R.id.cbOats)] = "Oats" to 2.49
        checkBoxes[findViewById(R.id.cbMilk)] = "Milk" to 1.89
        checkBoxes[findViewById(R.id.cbBananas)] = "Bananas" to 0.99
        checkBoxes[findViewById(R.id.cbChicken)] = "Chicken" to 6.49
        checkBoxes[findViewById(R.id.cbWrap)] = "Wrap" to 2.29
        checkBoxes[findViewById(R.id.cbPasta)] = "Pasta" to 1.59

        // Load saved checked states
        lifecycleScope.launch {
            for ((checkBox, itemData) in checkBoxes) {
                val itemName = itemData.first
                val isChecked = withContext(Dispatchers.IO) {
                    db.shoppingItemDao().isItemChecked(userEmail, itemName)
                }
                checkBox.isChecked = isChecked ?: false
            }
            updateSummary()
        }

        // Set listeners
        for ((checkBox, itemData) in checkBoxes) {
            val itemName = itemData.first
            val price = itemData.second

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val existing = db.shoppingItemDao().isItemChecked(userEmail, itemName)
                        if (existing == null) {
                            db.shoppingItemDao().insertItem(
                                ShoppingItemEntity(
                                    itemName = itemName,
                                    userEmail = userEmail,
                                    isChecked = isChecked,
                                    price = price
                                )
                            )
                        } else {
                            db.shoppingItemDao().updateChecked(userEmail, itemName, isChecked)
                        }
                    }
                    Log.d(TAG, "$itemName -> $isChecked")
                    updateSummary()
                }
            }
        }

        btnBack.setOnClickListener { finish() }

        btnClearList.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.shoppingItemDao().uncheckAll(userEmail)
                }
                for ((cb, _) in checkBoxes) {
                    cb.isChecked = false
                }
                updateSummary()
                Toast.makeText(this@ShoppingListActivity, "List cleared!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSummary() {
        var pickedCount = 0
        var totalPrice = 0.0

        for ((checkBox, itemData) in checkBoxes) {
            if (checkBox.isChecked) {
                pickedCount++
                totalPrice += itemData.second
            }
        }

        tvItemsPicked.text = "$pickedCount of ${checkBoxes.size} items picked"
        tvTotal.text = "R %.2f".format(totalPrice)
    }
}
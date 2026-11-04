package com.example.mealmate

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

// BaseActivity - forces the app to use the saved language whenever a screen loads
open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Get the saved language from SharedPreferences
        val prefs = newBase.getSharedPreferences("MealMatePrefs", Context.MODE_PRIVATE)
        val langCode = prefs.getString("app_language", "en") ?: "en"

        // Apply the language to the context
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        // Create a new context with the updated configuration
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
}
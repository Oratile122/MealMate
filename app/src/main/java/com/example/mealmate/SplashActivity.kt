package com.example.mealmate

// SplashActivity - first screen shown when the app opens
// Displays logo and loading indicator for 2 seconds
// Then automatically navigates to LoginActivity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    // Called when the splash screen is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Wait 2 seconds, then go to Login
        // Handler runs the code after a delay on the main thread
        Handler(Looper.getMainLooper()).postDelayed({
            // Create intent to open LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            // Close SplashActivity so pressing Back doesn't return here
            finish()
        }, 2000)   // 2000 milliseconds = 2 seconds
    }
}
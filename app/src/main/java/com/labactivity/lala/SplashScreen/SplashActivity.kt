package com.labactivity.lala.SplashScreen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.R
import com.labactivity.lala.UTILS.AuthManager

class SplashActivity : AppCompatActivity() {

    private val splashDelay = 2500L // 2.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logo_preview)
    }

    override fun onResume() {
        super.onResume()

        // Check authentication state and navigate accordingly after splash delay
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing) {
                // Use AuthManager to handle navigation based on auth state
                AuthManager.navigateBasedOnAuthState(this, finishCurrent = true)
            }
        }, splashDelay)
    }
}

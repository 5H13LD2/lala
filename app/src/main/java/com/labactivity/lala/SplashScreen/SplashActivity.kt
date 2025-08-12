package com.labactivity.lala.SplashScreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.R
import com.labactivity.lala.LOGINPAGE.MainActivity

class SplashActivity : AppCompatActivity() {

    private val splashDelay = 2500L // 2.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logo_preview)
    }

    override fun onResume() {
        super.onResume()

        // Siguraduhin na magla-launch lang habang nasa foreground pa
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, splashDelay)
    }
}

package com.labactivity.lala

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity6 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main6)

        // Get module information from intent
        val moduleId = intent.getStringExtra("module_id") ?: ""
        val moduleTitle = intent.getStringExtra("module_title") ?: "Python Quiz"

        // Update the welcome title with the module title
        val welcomeTitle = findViewById<TextView>(R.id.welcome_title)
        welcomeTitle.text = moduleTitle

        val startButton = findViewById<Button>(R.id.start_button)
        startButton.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java).apply {
                // Pass module information to QuizActivity
                putExtra("module_id", moduleId)
                putExtra("module_title", moduleTitle)
            }
            startActivity(intent)
        }
    }
}

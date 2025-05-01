package com.labactivity.lala

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ActivityMainQuiz : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_module)

        // Reference to the button
        val btnTakeQuizzes: Button = findViewById(R.id.btnTakeQuizzes)

        // Set click listener to navigate to MainActivity6
        btnTakeQuizzes.setOnClickListener {
            val intent = Intent(this, MainActivity6::class.java)
            startActivity(intent)
        }
    }
}

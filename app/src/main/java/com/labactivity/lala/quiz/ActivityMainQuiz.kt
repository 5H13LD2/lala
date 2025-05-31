package com.labactivity.lala.quiz

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.R

class ActivityMainQuiz : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_module)

        // Reference to the button
        val btnTakeQuizzes: Button = findViewById(R.id.btnTakeQuizzes)
    }
}

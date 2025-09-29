package com.labactivity.lala.quiz
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.R

class ActivityMainQuiz : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_module)

        // Get module information from intent
        val moduleId = intent.getStringExtra("module_id") ?: return
        val moduleTitle = intent.getStringExtra("module_title") ?: "Quiz"
        val quizId = intent.getStringExtra("quiz_id") ?: "default_quiz"

        // Reference to the button
        val btnTakeQuizzes: Button = findViewById(R.id.btnTakeQuizzes)
        
        // Set click listener for the quiz button
        btnTakeQuizzes.setOnClickListener {
            val intent = Intent(this, DynamicQuizActivity::class.java).apply {
                putExtra("module_id", moduleId)
                putExtra("module_title", moduleTitle)
                putExtra("quiz_id", quizId)
            }
            startActivity(intent)
            finish() // Close this activity after starting the quiz
        }
    }
}
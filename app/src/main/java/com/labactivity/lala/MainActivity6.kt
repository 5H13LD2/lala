package com.labactivity.lala

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.quiz.DynamicQuizActivity
import com.labactivity.lala.quiz.ModuleQuizRepository

class MainActivity6 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main6)

        // Get module information from intent
        val moduleId = intent.getStringExtra("module_id") ?: ""
        val moduleTitle = intent.getStringExtra("module_title") ?: "Python Quiz"
        
        Log.d("MainActivity6", "Received module ID: $moduleId, Title: $moduleTitle")

        // Update the welcome title with the module title
        val welcomeTitle = findViewById<TextView>(R.id.welcome_title)
        welcomeTitle.text = moduleTitle
        
        // Update subtitle based on module
        val welcomeSubtitle = findViewById<TextView>(R.id.welcome_subtitle)
        welcomeSubtitle.text = "Test your knowledge on $moduleTitle!"
        
        // Verify module has questions in ModuleQuizRepository
        val quizRepository = ModuleQuizRepository()
        val questionCount = quizRepository.getQuestionCountForModule(moduleId)
        Log.d("MainActivity6", "Module $moduleId has $questionCount questions available")

        val startButton = findViewById<Button>(R.id.start_button)
        startButton.setOnClickListener {
            val intent = Intent(this, DynamicQuizActivity::class.java).apply {
                // Pass module information to DynamicQuizActivity
                putExtra("module_id", moduleId)
                putExtra("module_title", moduleTitle)
            }
            startActivity(intent)
        }
    }
}

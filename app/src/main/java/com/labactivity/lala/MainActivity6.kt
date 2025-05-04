package com.labactivity.lala

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.quiz.DynamicQuizActivity
import com.labactivity.lala.quiz.QuizRepositoryFactory

class MainActivity6 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main6)

        // Get module information from intent
        var moduleId = intent.getStringExtra("module_id") ?: ""
        val moduleTitle = intent.getStringExtra("module_title") ?: "Python Quiz"
        
        Log.d("MainActivity6", "Original module ID: $moduleId, Title: $moduleTitle")


        when {
            moduleTitle.contains("SQL", ignoreCase = true) && !moduleId.contains("sql") ->
                moduleId = "sql_$moduleId"
            moduleTitle.contains("Python", ignoreCase = true) && !moduleId.contains("python") ->
                moduleId = "python_$moduleId"
            moduleTitle.contains("Java", ignoreCase = true) && !moduleId.contains("java") ->
                moduleId = "java_$moduleId"
        }
        
        Log.d("MainActivity6", "Modified module ID: $moduleId")

        // Update the welcome title with the module title
        val welcomeTitle = findViewById<TextView>(R.id.welcome_title)
        welcomeTitle.text = moduleTitle
        
        // Update subtitle based on module
        val welcomeSubtitle = findViewById<TextView>(R.id.welcome_subtitle)
        welcomeSubtitle.text = "Test your knowledge on $moduleTitle!"
        
        // Get the appropriate repository for this module and verify it has questions
        val quizRepository = QuizRepositoryFactory.getRepositoryForModule(moduleId)
        val questionCount = quizRepository.getQuestionCountForModule(moduleId)
        Log.d("MainActivity6", "Using repository: ${quizRepository.javaClass.simpleName}")
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

package com.labactivity.lala.quiz

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.labactivity.lala.R
import com.labactivity.lala.ResultActivity
import java.util.concurrent.TimeUnit
import com.labactivity.lala.quiz.QuizRepository
import com.labactivity.lala.quiz.QuizRepositoryFactory

class DynamicQuizActivity : AppCompatActivity() {

    // UI components
    private lateinit var tvQuestion: TextView
    private lateinit var tvQuestionNumber: TextView
    private lateinit var tvTimer: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnNext: Button
    
    // Data
    private lateinit var quizRepository: QuizRepository
    private lateinit var moduleId: String
    private lateinit var moduleTitle: String
    private lateinit var questions: List<Quiz>
    private var currentQuestionIndex = 0
    private val userAnswers = mutableMapOf<String, Int>()
    
    // Timer
    private var countDownTimer: CountDownTimer? = null
    private val quizTimeInMillis: Long = 3 * 60 * 1000 // 3 minutes
    private var timeLeftInMillis: Long = quizTimeInMillis
    private var quizStartTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        
        // Initialize UI components
        tvQuestion = findViewById(R.id.question_text)
        tvQuestionNumber = findViewById(R.id.question_counter)
        tvTimer = findViewById(R.id.timer_text)
        radioGroup = findViewById(R.id.answer_options)
        btnNext = findViewById(R.id.next_button)
        
        // Get module information from intent
        moduleId = intent.getStringExtra("module_id") ?: "1"
        moduleTitle = intent.getStringExtra("module_title") ?: "Python Quiz"
        
        Log.d("DynamicQuizActivity", "Original Module ID: $moduleId, Title: $moduleTitle")
        
        // Apply prefix logic BEFORE repository selection
        when {
            moduleTitle.contains("SQL", ignoreCase = true) && !moduleId.contains("sql") ->
                moduleId = "sql_$moduleId"
            moduleTitle.contains("Python", ignoreCase = true) && !moduleId.contains("python") ->
                moduleId = "python_$moduleId"
            moduleTitle.contains("Java", ignoreCase = true) && !moduleId.contains("java") ->
                moduleId = "java_$moduleId"
        }
        
        Log.d("DynamicQuizActivity", "Modified Module ID: $moduleId")
        
        // Set up title
        title = moduleTitle
        
        // Get the appropriate repository for this module using the factory
        quizRepository = QuizRepositoryFactory.getRepositoryForModule(moduleId)
        Log.d("DynamicQuizActivity", "Using repository: ${quizRepository.javaClass.simpleName}")
        
        // Get questions for this module
        questions = quizRepository.getQuestionsForModule(moduleId)
        
        Log.d("DynamicQuizActivity", "Retrieved ${questions.size} questions for module $moduleId")
        
        if (questions.isEmpty()) {
            // No questions found for this module
            Log.e("DynamicQuizActivity", "No questions found for module $moduleId")
            showNoQuestionsError()
            return
        }
        
        // Start the quiz
        quizStartTime = System.currentTimeMillis()
        startTimer()
        displayCurrentQuestion()
        
        // Set up navigation buttons
        btnNext.setOnClickListener {
            saveAnswer()
            if (currentQuestionIndex < questions.size - 1) {
                // Move to next question
                currentQuestionIndex++
                displayCurrentQuestion()
            } else {
                // End of quiz
                finishQuiz()
            }
        }
    }
    
    private fun showNoQuestionsError() {
        tvQuestion.text = "No questions available for this module."
        tvQuestionNumber.visibility = View.GONE
        radioGroup.visibility = View.GONE
        btnNext.text = "Go back"
        btnNext.setOnClickListener { finish() }
    }
    
    private fun displayCurrentQuestion() {
        val currentQuestion = questions[currentQuestionIndex]
        
        // Update question text
        tvQuestion.text = currentQuestion.question
        
        // Update question counter
        tvQuestionNumber.text = "Question ${currentQuestionIndex + 1} of ${questions.size}"
        
        // Clear and update radio options
        radioGroup.removeAllViews()
        
        // Add radio buttons for each option
        currentQuestion.options.forEachIndexed { index, option ->
            val radioButton = RadioButton(this).apply {
                id = index
                text = option
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            radioGroup.addView(radioButton)
        }
        
        // Check selected option if user has already answered
        userAnswers[currentQuestion.id]?.let { selectedIndex ->
            val radioButton = radioGroup.getChildAt(selectedIndex) as? RadioButton
            radioButton?.isChecked = true
        }
        
        // Update button text
        btnNext.text = if (currentQuestionIndex == questions.size - 1) "Finish" else "Next"
    }
    
    private fun saveAnswer() {
        val currentQuestion = questions[currentQuestionIndex]
        val selectedId = radioGroup.checkedRadioButtonId
        
        if (selectedId != -1) {
            userAnswers[currentQuestion.id] = selectedId
        }
    }
    
    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }
            
            override fun onFinish() {
                timeLeftInMillis = 0
                updateTimerText()
                finishQuiz()
            }
        }.start()
    }
    
    private fun updateTimerText() {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) % 60
        
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        tvTimer.text = timeFormatted
        
        // Change timer color when less than 1 minute remains
        if (timeLeftInMillis < 1 * 60 * 1000) {
            tvTimer.setTextColor(ContextCompat.getColor(this, R.color.error_red))
        }
    }
    
    private fun finishQuiz() {
        // Cancel timer
        countDownTimer?.cancel()
        
        // Calculate score
        var score = 0
        var correctAnswers = 0
        var incorrectAnswers = 0
        var skippedQuestions = 0
        
        for (question in questions) {
            val userAnswer = userAnswers[question.id]
            
            if (userAnswer == null) {
                skippedQuestions++
            } else if (userAnswer == question.correctOptionIndex) {
                score++
                correctAnswers++
            } else {
                incorrectAnswers++
            }
        }
        
        // Calculate time taken
        val timeTaken = System.currentTimeMillis() - quizStartTime
        
        // Launch result activity
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("SCORE", score)
            putExtra("TOTAL", questions.size)
            putExtra("MODULE_ID", moduleId)
            putExtra("MODULE_TITLE", moduleTitle)
            putExtra("CORRECT_ANSWERS", correctAnswers)
            putExtra("INCORRECT_ANSWERS", incorrectAnswers)
            putExtra("SKIPPED_QUESTIONS", skippedQuestions)
            putExtra("TIME_TAKEN", timeTaken)
        }
        startActivity(intent)
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
} 
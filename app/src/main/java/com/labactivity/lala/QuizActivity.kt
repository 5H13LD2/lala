package com.labactivity.lala

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.quiz.QuizRepository
import com.labactivity.lala.quiz.QuizRepositoryFactory
import com.labactivity.lala.quiz.Quiz

class QuizActivity : AppCompatActivity() {

    private lateinit var questionTextView: TextView
    private lateinit var questionCounterTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var timeUpMessageTextView: TextView
    private lateinit var optionsRadioGroup: RadioGroup
    private lateinit var nextButton: Button

    private lateinit var quizRepository: QuizRepository
    private lateinit var questions: List<Quiz>
    private var currentQuestionIndex = 0
    private val userAnswers = mutableMapOf<String, Int>() // Map of questionId to selected answer

    private var countDownTimer: CountDownTimer? = null
    private val startTimeInMillis: Long = 225000 // 3:45 in milliseconds

    private lateinit var quizScoreManager: QuizScoreManager
    private var moduleId: String = ""
    private var moduleTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // Initialize the score manager
        quizScoreManager = QuizScoreManager(this)
        
        // Get module info from intent
        moduleId = intent.getStringExtra("module_id") ?: ""
        moduleTitle = intent.getStringExtra("module_title") ?: ""
        
        Log.d("QuizActivity", "Original Module ID: $moduleId, Title: $moduleTitle")
        
        // Apply prefix logic BEFORE repository selection
        when {
            moduleTitle.contains("SQL", ignoreCase = true) && !moduleId.contains("sql") ->
                moduleId = "sql_$moduleId"
            moduleTitle.contains("Python", ignoreCase = true) && !moduleId.contains("python") ->
                moduleId = "python_$moduleId"
            moduleTitle.contains("Java", ignoreCase = true) && !moduleId.contains("java") ->
                moduleId = "java_$moduleId"
        }
        
        Log.d("QuizActivity", "Modified Module ID: $moduleId")

        // Initialize views
        initializeViews()
        
        // Get the appropriate repository for this module using the factory
        quizRepository = QuizRepositoryFactory.getRepositoryForModule(moduleId)
        Log.d("QuizActivity", "Using repository: ${quizRepository.javaClass.simpleName}")
        
        // Get questions for this module
        questions = quizRepository.getQuestionsForModule(moduleId)
        
        Log.d("QuizActivity", "Retrieved ${questions.size} questions for module $moduleId")
        
        if (questions.isEmpty()) {
            // No questions found for this module
            Log.e("QuizActivity", "No questions found for module $moduleId")
            showNoQuestionsError()
            return
        }
        
        // Start the quiz
        startTimer()
        displayQuestion()

        nextButton.setOnClickListener {
            saveAnswer()
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                displayQuestion()
            } else {
                finishQuiz()
            }
        }
    }

    private fun showNoQuestionsError() {
        questionTextView.text = "No questions available for this module."
        questionCounterTextView.visibility = View.GONE
        optionsRadioGroup.visibility = View.GONE
        nextButton.text = "Go back"
        nextButton.setOnClickListener { finish() }
    }

    private fun initializeViews() {
        questionTextView = findViewById(R.id.question_text)
        questionCounterTextView = findViewById(R.id.question_counter)
        timerTextView = findViewById(R.id.timer_text)
        timeUpMessageTextView = findViewById(R.id.time_up_message)
        optionsRadioGroup = findViewById(R.id.answer_options)
        nextButton = findViewById(R.id.next_button)
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(startTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                val timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
                timerTextView.text = timeLeftFormatted

                if (millisUntilFinished < 60000) {
                    timerTextView.setTextColor(Color.parseColor("#FF4500")) // Orange
                } else {
                    timerTextView.setTextColor(Color.parseColor("#1E90FF")) // Blue
                }
            }

            override fun onFinish() {
                timerTextView.text = "00:00"
                showTimeUpMessage()
                finishQuiz()
            }
        }.start()
    }

    private fun showTimeUpMessage() {
        timeUpMessageTextView.visibility = View.VISIBLE
    }

    private fun displayQuestion() {
        val question = questions[currentQuestionIndex]
        questionTextView.text = question.question
        questionCounterTextView.text = "Question ${currentQuestionIndex + 1} of ${questions.size}"

        // Clear previous radio buttons
        optionsRadioGroup.removeAllViews()
        
        // Add radio buttons for each option
        question.options.forEachIndexed { index, option ->
            val radioButton = RadioButton(this).apply {
                id = index
                text = option
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            optionsRadioGroup.addView(radioButton)
        }
        
        // Check selected option if user has already answered
        userAnswers[question.id]?.let { selectedIndex ->
            val radioButton = optionsRadioGroup.getChildAt(selectedIndex) as? RadioButton
            radioButton?.isChecked = true
        }

        if (currentQuestionIndex == questions.size - 1) {
            nextButton.text = "Finish"
        } else {
            nextButton.text = "Next"
        }
    }

    private fun saveAnswer() {
        val currentQuestion = questions[currentQuestionIndex]
        val selectedId = optionsRadioGroup.checkedRadioButtonId
        
        if (selectedId != -1) {
            userAnswers[currentQuestion.id] = selectedId
        }
    }

    private fun finishQuiz() {
        countDownTimer?.cancel()

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

        // Save the score for this module
        if (moduleId.isNotEmpty()) {
            quizScoreManager.saveQuizScore(moduleId, score, questions.size)
        }

        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("SCORE", score)
        intent.putExtra("TOTAL", questions.size)
        intent.putExtra("MODULE_ID", moduleId)
        intent.putExtra("MODULE_TITLE", moduleTitle)
        intent.putExtra("CORRECT_ANSWERS", correctAnswers)
        intent.putExtra("INCORRECT_ANSWERS", incorrectAnswers)
        intent.putExtra("SKIPPED_QUESTIONS", skippedQuestions)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
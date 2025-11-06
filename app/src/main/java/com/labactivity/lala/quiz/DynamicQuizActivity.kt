package com.labactivity.lala.quiz

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.labactivity.lala.R
import com.labactivity.lala.REVIEWER.ResultActivity

class DynamicQuizActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DynamicQuizActivity"
        private const val QUIZ_TIME_MINUTES = 15L
        private const val MILLIS_IN_SECOND = 1000L
    }

    // UI Components
    private lateinit var questionCounter: TextView
    private lateinit var timerText: TextView
    private lateinit var questionText: TextView
    private lateinit var answerOptions: RadioGroup
    private lateinit var nextButton: Button
    private lateinit var timeUpMessage: TextView
    private lateinit var backButton: ImageButton
    private lateinit var option1: RadioButton
    private lateinit var option2: RadioButton
    private lateinit var option3: RadioButton
    private lateinit var option4: RadioButton

    // Data
    private val quizList = mutableListOf<Quiz>()
    private val userAnswers = mutableMapOf<Int, Int>() // questionIndex to selectedOptionIndex
    private var currentQuestionIndex = 0

    // Intent extras
    private var moduleId: String = ""
    private var moduleTitle: String = ""
    private var quizId: String = ""

    // Timer
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = QUIZ_TIME_MINUTES * 60 * MILLIS_IN_SECOND
    private var quizStartTime: Long = 0L  // Track when quiz actually started

    // Firestore
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "onCreate: DynamicQuizActivity started")
        Log.d(TAG, "═══════════════════════════════════════")

        initializeViews()
        extractIntentData()
        loadQuizQuestions()
    }

    private fun initializeViews() {
        Log.d(TAG, "initializeViews: Binding UI components")

        questionCounter = findViewById(R.id.question_counter)
        timerText = findViewById(R.id.timer_text)
        questionText = findViewById(R.id.question_text)
        answerOptions = findViewById(R.id.answer_options)
        nextButton = findViewById(R.id.next_button)
        timeUpMessage = findViewById(R.id.time_up_message)
        backButton = findViewById(R.id.backButton)
        option1 = findViewById(R.id.option_1)
        option2 = findViewById(R.id.option_2)
        option3 = findViewById(R.id.option_3)
        option4 = findViewById(R.id.option_4)

        nextButton.setOnClickListener { handleNextButton() }
        backButton.setOnClickListener {
            countDownTimer?.cancel()
            finish()
        }

        Log.d(TAG, "initializeViews: All views initialized successfully")
    }

    private fun extractIntentData() {
        Log.d(TAG, "─────────────────────────────────────")
        Log.d(TAG, "extractIntentData: Extracting intent extras")

        moduleId = intent.getStringExtra("module_id") ?: ""
        moduleTitle = intent.getStringExtra("module_title") ?: "Quiz"
        quizId = intent.getStringExtra("quiz_id") ?: "default_quiz"

        Log.d(TAG, "extractIntentData: ✓ moduleId = '$moduleId'")
        Log.d(TAG, "extractIntentData: ✓ moduleTitle = '$moduleTitle'")
        Log.d(TAG, "extractIntentData: ✓ quizId = '$quizId'")

        // Validation
        if (moduleId.isEmpty()) {
            Log.e(TAG, "extractIntentData: ✗ ERROR - moduleId is EMPTY!")
            Toast.makeText(this, "Error: No module ID provided", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (quizId.isEmpty()) {
            Log.e(TAG, "extractIntentData: ✗ ERROR - quizId is EMPTY!")
            Toast.makeText(this, "Error: No quiz ID provided", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        Log.d(TAG, "extractIntentData: ✓ All intent data validated successfully")
        Log.d(TAG, "─────────────────────────────────────")
    }

    private fun loadQuizQuestions() {
        firestore.collection("course_quiz")
            .document(quizId)
            .collection("questions")
            .whereEqualTo("module_id", moduleId)
            .orderBy("order", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "loadQuizQuestions: ✓ Firestore query SUCCEEDED")
                Log.d(TAG, "loadQuizQuestions: Found ${documents.size()} documents")
                Log.d(TAG, "─────────────────────────────────────")

                if (documents.isEmpty) {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "No questions found for this module.\nModule: $moduleTitle",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                    return@addOnSuccessListener
                }

                val allQuestions = mutableListOf<Quiz>()

                documents.forEachIndexed { index, document ->
                    try {
                        val quiz = document.toObject(Quiz::class.java)

                        if (quiz.question.isEmpty() || quiz.options.size < 2) {
                            Log.w(TAG, "⚠ Invalid quiz data at index $index")
                        } else {
                            allQuestions.add(quiz)
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "✗ ERROR parsing document to Quiz object", e)
                    }
                }

                if (allQuestions.isEmpty()) {
                    Log.e(TAG, "loadQuizQuestions: ✗ ERROR - All documents failed to parse!")
                    runOnUiThread {
                        Toast.makeText(this, "Error loading quiz questions", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    return@addOnSuccessListener
                }

                // ✅ Shuffle and take 20 random questions
                quizList.clear()
                quizList.addAll(allQuestions.shuffled().take(20))

                Log.d(TAG, "loadQuizQuestions: ✓ Loaded ${quizList.size} random questions")
                Log.d(TAG, "═══════════════════════════════════════")

                runOnUiThread {
                    displayQuestion()
                    startTimer()
                }
            }
            .addOnFailureListener { exception ->
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Error loading quiz: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
    }


    private fun displayQuestion() {
        if (currentQuestionIndex >= quizList.size) {
            Log.w(TAG, "displayQuestion: Attempted to display question beyond quiz size")
            return
        }

        val quiz = quizList[currentQuestionIndex]



        // Update UI
        questionCounter.text = "Question ${currentQuestionIndex + 1} of ${quizList.size}"
        questionText.text = quiz.question

        // Clear previous selection
        answerOptions.clearCheck()

        // Set options (handle variable number of options)
        val radioButtons = listOf(option1, option2, option3, option4)
        radioButtons.forEachIndexed { index, radioButton ->
            if (index < quiz.options.size) {
                radioButton.text = quiz.options[index]
                radioButton.visibility = View.VISIBLE
                Log.d(TAG, "  Option ${index + 1}: ${quiz.options[index]}")
            } else {
                radioButton.visibility = View.GONE
            }
        }

        // Restore previous answer if exists
        userAnswers[currentQuestionIndex]?.let { savedAnswer ->
            Log.d(TAG, "  ↻ Restoring previous answer: Option ${savedAnswer + 1}")
            radioButtons[savedAnswer].isChecked = true
        }

        // Update button text
        nextButton.text = if (currentQuestionIndex == quizList.size - 1) "Finish" else "Next"

        Log.d(TAG, "displayQuestion: ✓ Question displayed successfully")
        Log.d(TAG, "─────────────────────────────────────")
    }

    private fun handleNextButton() {
        Log.d(TAG, "─────────────────────────────────────")
        Log.d(TAG, "handleNextButton: Processing question ${currentQuestionIndex + 1}")

        val selectedId = answerOptions.checkedRadioButtonId

        if (selectedId != -1) {
            val selectedOptionIndex = when (selectedId) {
                R.id.option_1 -> 0
                R.id.option_2 -> 1
                R.id.option_3 -> 2
                R.id.option_4 -> 3
                else -> -1
            }

            if (selectedOptionIndex != -1) {
                userAnswers[currentQuestionIndex] = selectedOptionIndex
                val quiz = quizList[currentQuestionIndex]
                val isCorrect = selectedOptionIndex == quiz.correctOptionIndex


            }
        } else {
            Log.d(TAG, "  ⊘ No option selected (will be marked as skipped)")
        }

        currentQuestionIndex++

        if (currentQuestionIndex < quizList.size) {
            Log.d(TAG, "  → Moving to next question")
            displayQuestion()
        } else {
            Log.d(TAG, "  ✓ Quiz completed - showing results")
            showResults()
        }

        Log.d(TAG, "─────────────────────────────────────")
    }

    private fun startTimer() {
        Log.d(TAG, "startTimer: Starting ${QUIZ_TIME_MINUTES} minute countdown")

        // Record quiz start time
        quizStartTime = System.currentTimeMillis()

        countDownTimer = object : CountDownTimer(timeLeftInMillis, MILLIS_IN_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerUI()
            }

            override fun onFinish() {
                Log.w(TAG, "startTimer: ⏰ TIME'S UP!")
                timeUpMessage.visibility = View.VISIBLE
                nextButton.isEnabled = false

                android.os.Handler(mainLooper).postDelayed({
                    showResults()
                }, 2000)
            }
        }.start()
    }

    private fun updateTimerUI() {
        val minutes = (timeLeftInMillis / MILLIS_IN_SECOND) / 60
        val seconds = (timeLeftInMillis / MILLIS_IN_SECOND) % 60
        timerText.text = String.format("%02d:%02d", minutes, seconds)

        // Change color if time is running out
        if (minutes == 0L && seconds <= 30) {
            timerText.setTextColor(getColor(android.R.color.holo_red_dark))
        }
    }

    private fun showResults() {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "showResults: Calculating final score")
        Log.d(TAG, "═══════════════════════════════════════")

        var correctCount = 0
        var incorrectCount = 0
        var skippedCount = 0

        quizList.forEachIndexed { index, quiz ->
            val userAnswer = userAnswers[index]

            when {
                userAnswer == null -> {
                    skippedCount++
                    Log.d(TAG, "Q${index + 1}: ⊘ SKIPPED")
                }
                userAnswer == quiz.correctOptionIndex -> {
                    correctCount++
                    Log.d(TAG, "Q${index + 1}: ✓ CORRECT")
                }
                else -> {
                    incorrectCount++
                    Log.d(TAG, "Q${index + 1}: ✗ INCORRECT (selected: ${userAnswer + 1}, correct: ${quiz.correctOptionIndex + 1})")
                }
            }
        }



        countDownTimer?.cancel()

        // Calculate time taken (in milliseconds)
        val timeTaken = if (quizStartTime > 0) {
            System.currentTimeMillis() - quizStartTime
        } else {
            0L
        }

        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("SCORE", correctCount)
            putExtra("TOTAL", quizList.size)
            putExtra("MODULE_ID", moduleId)
            putExtra("MODULE_TITLE", moduleTitle)
            putExtra("QUIZ_ID", quizId)
            putExtra("TIME_TAKEN", timeTaken)  // Pass time taken
            putParcelableArrayListExtra("QUIZ_QUESTIONS", ArrayList(quizList))
        }

        Log.d(TAG, "showResults: Navigating to ResultActivity with ${quizList.size} questions")
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        Log.d(TAG, "onDestroy: DynamicQuizActivity destroyed, timer cancelled")
    }
}
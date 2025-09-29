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
        option1 = findViewById(R.id.option_1)
        option2 = findViewById(R.id.option_2)
        option3 = findViewById(R.id.option_3)
        option4 = findViewById(R.id.option_4)

        nextButton.setOnClickListener { handleNextButton() }

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
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "loadQuizQuestions: Starting Firestore query")
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "Query details:")
        Log.d(TAG, "  Collection path: course_quiz/$quizId/questions")
        Log.d(TAG, "  Filter: module_id == '$moduleId'")
        Log.d(TAG, "  Order by: order ASC")
        Log.d(TAG, "─────────────────────────────────────")

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
                    Log.e(TAG, "loadQuizQuestions: ✗ WARNING - No documents found!")
                    Log.e(TAG, "Possible causes:")
                    Log.e(TAG, "  1. module_id '$moduleId' doesn't exist in any questions")
                    Log.e(TAG, "  2. quiz_id '$quizId' doesn't exist")
                    Log.e(TAG, "  3. No questions added to this module yet")
                    Log.e(TAG, "  4. Firestore rules preventing read access")
                    Log.e(TAG, "  5. Missing composite index (module_id + order)")

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

                documents.forEachIndexed { index, document ->
                    Log.d(TAG, "─────────────────────────────────────")
                    Log.d(TAG, "Processing document #${index + 1}:")
                    Log.d(TAG, "  Document ID: ${document.id}")
                    Log.d(TAG, "  Document path: ${document.reference.path}")

                    // Log all fields in the document
                    Log.d(TAG, "  Raw document data:")
                    document.data?.forEach { (key, value) ->
                        Log.d(TAG, "    $key: $value (${value?.javaClass?.simpleName})")
                    }

                    try {
                        val quiz = document.toObject(Quiz::class.java)

                        // Validate parsed quiz object
                        Log.d(TAG, "  ✓ Parsed Quiz object:")
                        Log.d(TAG, "    question: ${quiz.question}")
                        Log.d(TAG, "    options: ${quiz.options}")
                        Log.d(TAG, "    correctOptionIndex: ${quiz.correctOptionIndex}")
                        Log.d(TAG, "    difficulty: ${quiz.difficulty}")
                        Log.d(TAG, "    order: ${quiz.order}")
                        Log.d(TAG, "    module_id: ${quiz.module_id}")

                        // Validation checks
                        if (quiz.question.isEmpty()) {
                            Log.w(TAG, "    ⚠ WARNING: Question text is empty!")
                        }
                        if (quiz.options.size < 2) {
                            Log.w(TAG, "    ⚠ WARNING: Less than 2 options (${quiz.options.size})")
                        }
                        if (quiz.correctOptionIndex < 0 || quiz.correctOptionIndex >= quiz.options.size) {
                            Log.w(TAG, "    ⚠ WARNING: Invalid correctOptionIndex (${quiz.correctOptionIndex})")
                        }
                        if (quiz.module_id != moduleId) {
                            Log.w(TAG, "    ⚠ WARNING: module_id mismatch!")
                            Log.w(TAG, "      Expected: '$moduleId'")
                            Log.w(TAG, "      Got: '${quiz.module_id}'")
                        }

                        quizList.add(quiz)
                        Log.d(TAG, "  ✓ Successfully added to quizList")

                    } catch (e: Exception) {
                        Log.e(TAG, "  ✗ ERROR parsing document to Quiz object", e)
                        Log.e(TAG, "    Exception: ${e.javaClass.simpleName}")
                        Log.e(TAG, "    Message: ${e.message}")
                    }
                }

                Log.d(TAG, "─────────────────────────────────────")
                Log.d(TAG, "loadQuizQuestions: Parsing complete")
                Log.d(TAG, "  Total documents: ${documents.size()}")
                Log.d(TAG, "  Successfully parsed: ${quizList.size}")
                Log.d(TAG, "  Failed to parse: ${documents.size() - quizList.size}")

                if (quizList.isEmpty()) {
                    Log.e(TAG, "loadQuizQuestions: ✗ ERROR - All documents failed to parse!")
                    runOnUiThread {
                        Toast.makeText(this, "Error loading quiz questions", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    return@addOnSuccessListener
                }

                Log.d(TAG, "loadQuizQuestions: ✓ Starting quiz with ${quizList.size} questions")
                Log.d(TAG, "═══════════════════════════════════════")

                runOnUiThread {
                    displayQuestion()
                    startTimer()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "loadQuizQuestions: ✗ Firestore query FAILED")
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "Exception type: ${exception.javaClass.simpleName}")
                Log.e(TAG, "Error message: ${exception.message}")
                Log.e(TAG, "Stack trace:", exception)
                Log.e(TAG, "─────────────────────────────────────")
                Log.e(TAG, "Troubleshooting steps:")
                Log.e(TAG, "  1. Check Firestore Console for the collection path")
                Log.e(TAG, "  2. Verify Firestore security rules allow read access")
                Log.e(TAG, "  3. Create composite index if needed:")
                Log.e(TAG, "     Collection: course_quiz/$quizId/questions")
                Log.e(TAG, "     Fields: module_id (ASC) + order (ASC)")
                Log.e(TAG, "  4. Check device internet connection")
                Log.e(TAG, "  5. Verify Firebase project is correctly configured")
                Log.e(TAG, "═══════════════════════════════════════")

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

        Log.d(TAG, "─────────────────────────────────────")
        Log.d(TAG, "displayQuestion: Showing question ${currentQuestionIndex + 1}/${quizList.size}")
        Log.d(TAG, "  Question: ${quiz.question}")
        Log.d(TAG, "  Options: ${quiz.options}")
        Log.d(TAG, "  Correct index: ${quiz.correctOptionIndex}")
        Log.d(TAG, "  Difficulty: ${quiz.difficulty}")

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

                Log.d(TAG, "  User selected: Option ${selectedOptionIndex + 1}")
                Log.d(TAG, "  Correct answer: Option ${quiz.correctOptionIndex + 1}")
                Log.d(TAG, "  Result: ${if (isCorrect) "✓ CORRECT" else "✗ INCORRECT"}")
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

        Log.d(TAG, "─────────────────────────────────────")
        Log.d(TAG, "Final Results:")
        Log.d(TAG, "  Total questions: ${quizList.size}")
        Log.d(TAG, "  ✓ Correct: $correctCount")
        Log.d(TAG, "  ✗ Incorrect: $incorrectCount")
        Log.d(TAG, "  ⊘ Skipped: $skippedCount")
        Log.d(TAG, "  Score: $correctCount/${quizList.size}")
        Log.d(TAG, "  Percentage: ${(correctCount * 100.0 / quizList.size).toInt()}%")
        Log.d(TAG, "  Pass threshold: 70%")
        Log.d(TAG, "  Status: ${if (correctCount >= quizList.size * 0.7) "PASSED ✓" else "FAILED ✗"}")
        Log.d(TAG, "═══════════════════════════════════════")

        countDownTimer?.cancel()

        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("SCORE", correctCount)
            putExtra("TOTAL", quizList.size)
            putExtra("MODULE_ID", moduleId)
            putExtra("MODULE_TITLE", moduleTitle)
        }

        Log.d(TAG, "showResults: Navigating to ResultActivity")
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        Log.d(TAG, "onDestroy: DynamicQuizActivity destroyed, timer cancelled")
    }
}
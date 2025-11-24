package com.labactivity.lala.quiz

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.labactivity.lala.UTILS.DialogUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DynamicQuizActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DynamicQuizActivity"
        private const val QUIZ_TIME_MINUTES = 15L
        private const val MILLIS_IN_SECOND = 1000L

        // SharedPreferences keys
        private const val PREFS_NAME = "QuizPreferences"
        private const val KEY_QUIZ_IN_PROGRESS = "quiz_in_progress"
        private const val KEY_TIME_LEFT = "time_left"
        private const val KEY_CURRENT_QUESTION = "current_question"
        private const val KEY_USER_ANSWERS = "user_answers"
        private const val KEY_QUIZ_QUESTIONS = "quiz_questions"
        private const val KEY_MODULE_ID = "module_id"
        private const val KEY_MODULE_TITLE = "module_title"
        private const val KEY_QUIZ_ID = "quiz_id"
        private const val KEY_QUIZ_START_TIME = "quiz_start_time"
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

    // SharedPreferences for quiz state persistence
    private lateinit var quizPrefs: SharedPreferences
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "onCreate: DynamicQuizActivity started")
        Log.d(TAG, "═══════════════════════════════════════")

        // Initialize SharedPreferences
        quizPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        initializeViews()
        extractIntentData()

        // Check if there's a saved quiz state to resume
        if (hasSavedQuizState()) {
            Log.d(TAG, "onCreate: Found saved quiz state - offering to resume")
            showResumeQuizDialog()
        } else {
            loadQuizQuestions()
        }
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
            showExitConfirmationDialog()
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
            DialogUtils.showErrorDialog(this, "Error", "No module ID provided")
            finish()
            return
        }

        if (quizId.isEmpty()) {
            Log.e(TAG, "extractIntentData: ✗ ERROR - quizId is EMPTY!")
            DialogUtils.showErrorDialog(this, "Error", "No quiz ID provided")
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
                        DialogUtils.showErrorDialog(
                            this,
                            "No Questions",
                            "No questions found for this module.\nModule: $moduleTitle"
                        )
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
                        DialogUtils.showErrorDialog(this, "Error", "Error loading quiz questions")
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
                    DialogUtils.showErrorDialog(
                        this,
                        "Error",
                        "Error loading quiz: ${exception.message}"
                    )
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

        // Save quiz state after each question
        saveQuizState()

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

        // Save initial quiz state
        saveQuizState()

        countDownTimer = object : CountDownTimer(timeLeftInMillis, MILLIS_IN_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerUI()
                // Save state every 10 seconds
                if ((millisUntilFinished / MILLIS_IN_SECOND) % 10 == 0L) {
                    saveQuizState()
                }
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

        // Clear saved quiz state since quiz is completed
        clearQuizState()

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

    // ═══════════════════════════════════════
    // QUIZ STATE PERSISTENCE METHODS
    // ═══════════════════════════════════════

    private fun hasSavedQuizState(): Boolean {
        return quizPrefs.getBoolean(KEY_QUIZ_IN_PROGRESS, false)
    }

    private fun saveQuizState() {
        try {
            val editor = quizPrefs.edit()
            editor.putBoolean(KEY_QUIZ_IN_PROGRESS, true)
            editor.putLong(KEY_TIME_LEFT, timeLeftInMillis)
            editor.putInt(KEY_CURRENT_QUESTION, currentQuestionIndex)
            editor.putString(KEY_MODULE_ID, moduleId)
            editor.putString(KEY_MODULE_TITLE, moduleTitle)
            editor.putString(KEY_QUIZ_ID, quizId)
            editor.putLong(KEY_QUIZ_START_TIME, quizStartTime)

            // Save user answers as JSON
            val answersJson = gson.toJson(userAnswers)
            editor.putString(KEY_USER_ANSWERS, answersJson)

            // Save quiz questions as JSON
            val questionsJson = gson.toJson(quizList)
            editor.putString(KEY_QUIZ_QUESTIONS, questionsJson)

            editor.apply()
            Log.d(TAG, "saveQuizState: Quiz state saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "saveQuizState: Error saving quiz state", e)
        }
    }

    private fun restoreQuizState() {
        try {
            Log.d(TAG, "restoreQuizState: Restoring saved quiz state")

            // Restore basic data
            moduleId = quizPrefs.getString(KEY_MODULE_ID, "") ?: ""
            moduleTitle = quizPrefs.getString(KEY_MODULE_TITLE, "Quiz") ?: "Quiz"
            quizId = quizPrefs.getString(KEY_QUIZ_ID, "") ?: ""
            timeLeftInMillis = quizPrefs.getLong(KEY_TIME_LEFT, QUIZ_TIME_MINUTES * 60 * MILLIS_IN_SECOND)
            currentQuestionIndex = quizPrefs.getInt(KEY_CURRENT_QUESTION, 0)
            quizStartTime = quizPrefs.getLong(KEY_QUIZ_START_TIME, 0L)

            // Restore user answers
            val answersJson = quizPrefs.getString(KEY_USER_ANSWERS, null)
            if (answersJson != null) {
                val type = object : TypeToken<MutableMap<Int, Int>>() {}.type
                val restoredAnswers: MutableMap<Int, Int> = gson.fromJson(answersJson, type)
                userAnswers.clear()
                userAnswers.putAll(restoredAnswers)
            }

            // Restore quiz questions
            val questionsJson = quizPrefs.getString(KEY_QUIZ_QUESTIONS, null)
            if (questionsJson != null) {
                val type = object : TypeToken<MutableList<Quiz>>() {}.type
                val restoredQuestions: MutableList<Quiz> = gson.fromJson(questionsJson, type)
                quizList.clear()
                quizList.addAll(restoredQuestions)
            }

            Log.d(TAG, "restoreQuizState: Restored question ${currentQuestionIndex + 1}/${quizList.size}")
            Log.d(TAG, "restoreQuizState: Time left: ${timeLeftInMillis / 1000}s")
            Log.d(TAG, "restoreQuizState: Answers restored: ${userAnswers.size}")

            // Update UI and restart timer
            runOnUiThread {
                displayQuestion()
                startTimer()
            }
        } catch (e: Exception) {
            Log.e(TAG, "restoreQuizState: Error restoring quiz state", e)
            DialogUtils.showErrorDialog(this, "Error", "Failed to restore quiz. Starting fresh.")
            loadQuizQuestions()
        }
    }

    private fun clearQuizState() {
        try {
            quizPrefs.edit().clear().apply()
            Log.d(TAG, "clearQuizState: Quiz state cleared")
        } catch (e: Exception) {
            Log.e(TAG, "clearQuizState: Error clearing quiz state", e)
        }
    }

    private fun showResumeQuizDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Resume Quiz?")
            .setMessage("You have an unfinished quiz. Would you like to resume from where you left off?")
            .setPositiveButton("Resume") { _, _ ->
                restoreQuizState()
            }
            .setNegativeButton("Start Fresh") { _, _ ->
                clearQuizState()
                loadQuizQuestions()
            }
            .setCancelable(false)
            .show()
    }

    private fun showExitConfirmationDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Exit Quiz?")
            .setMessage("Your progress will be saved and you can resume later. Are you sure you want to exit?")
            .setPositiveButton("Exit & Save") { _, _ ->
                saveQuizState()
                countDownTimer?.cancel()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(true)
            .show()
    }

    // Override back button to show confirmation
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showExitConfirmationDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        Log.d(TAG, "onDestroy: DynamicQuizActivity destroyed, timer cancelled")
    }
}
package com.labactivity.lala.quiz

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.labactivity.lala.R
import com.labactivity.lala.REVIEWER.ResultActivity
import com.labactivity.lala.UTILS.DialogUtils

class DynamicQuizActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DynamicQuizActivity"
        private const val QUIZ_TIME_MINUTES = 15L
        private const val MILLIS_IN_SECOND = 1000L
        private const val PREFS_NAME = "QuizPreferences"
        private const val KEY_TIME_LEFT = "time_left"
        private const val KEY_CURRENT_QUESTION = "current_question"
        private const val KEY_USER_ANSWERS = "user_answers"
        private const val KEY_QUIZ_LIST = "quiz_list"
        private const val KEY_QUIZ_START_TIME = "quiz_start_time"
        private const val KEY_QUIZ_ID = "saved_quiz_id"
        private const val KEY_HAS_INTERACTED = "has_user_interacted"
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
    private var hasUserInteracted = false  // Track if user has answered any question

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

    // SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "onCreate: DynamicQuizActivity started")
        Log.d(TAG, "═══════════════════════════════════════")

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        initializeViews()
        extractIntentData()

        // Check if there's a saved quiz state
        if (hasSavedQuizState()) {
            showRestoreQuizDialog()
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
            showQuitConfirmationDialog()
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
            // Mark that user has started interacting with the quiz
            hasUserInteracted = true

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

        // Clear saved quiz state when quiz is completed
        clearSavedQuizState()

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

    override fun onBackPressed() {
        showQuitConfirmationDialog()
    }

    override fun onPause() {
        super.onPause()
        // Save quiz state when app goes to background ONLY if user has interacted
        if (quizList.isNotEmpty() && hasUserInteracted) {
            saveQuizState()
        }
    }

    // ═══════════════════════════════════════
    // Quiz State Persistence Methods
    // ═══════════════════════════════════════

    private fun hasSavedQuizState(): Boolean {
        val savedQuizId = sharedPreferences.getString(KEY_QUIZ_ID, null)
        return savedQuizId == quizId && sharedPreferences.contains(KEY_TIME_LEFT)
    }

    private fun saveQuizState() {
        Log.d(TAG, "saveQuizState: Saving quiz state to SharedPreferences")

        val editor = sharedPreferences.edit()
        editor.putLong(KEY_TIME_LEFT, timeLeftInMillis)
        editor.putInt(KEY_CURRENT_QUESTION, currentQuestionIndex)
        editor.putLong(KEY_QUIZ_START_TIME, quizStartTime)
        editor.putString(KEY_QUIZ_ID, quizId)
        editor.putBoolean(KEY_HAS_INTERACTED, hasUserInteracted)

        // Save user answers as JSON
        val answersJson = gson.toJson(userAnswers)
        editor.putString(KEY_USER_ANSWERS, answersJson)

        // Save quiz list as JSON
        val quizListJson = gson.toJson(quizList)
        editor.putString(KEY_QUIZ_LIST, quizListJson)

        editor.apply()
        Log.d(TAG, "saveQuizState: Quiz state saved successfully")
    }

    private fun restoreQuizState() {
        Log.d(TAG, "restoreQuizState: Restoring quiz state from SharedPreferences")

        timeLeftInMillis = sharedPreferences.getLong(KEY_TIME_LEFT, QUIZ_TIME_MINUTES * 60 * MILLIS_IN_SECOND)
        currentQuestionIndex = sharedPreferences.getInt(KEY_CURRENT_QUESTION, 0)
        quizStartTime = sharedPreferences.getLong(KEY_QUIZ_START_TIME, 0L)
        hasUserInteracted = sharedPreferences.getBoolean(KEY_HAS_INTERACTED, false)

        // Restore user answers
        val answersJson = sharedPreferences.getString(KEY_USER_ANSWERS, null)
        if (answersJson != null) {
            val type = object : TypeToken<MutableMap<Int, Int>>() {}.type
            val restoredAnswers: MutableMap<Int, Int> = gson.fromJson(answersJson, type)
            userAnswers.clear()
            userAnswers.putAll(restoredAnswers)
        }

        // Restore quiz list
        val quizListJson = sharedPreferences.getString(KEY_QUIZ_LIST, null)
        if (quizListJson != null) {
            val type = object : TypeToken<MutableList<Quiz>>() {}.type
            val restoredQuizList: MutableList<Quiz> = gson.fromJson(quizListJson, type)
            quizList.clear()
            quizList.addAll(restoredQuizList)
        }

        Log.d(TAG, "restoreQuizState: Quiz state restored - Question ${currentQuestionIndex + 1}/${quizList.size}, Time: ${timeLeftInMillis / 1000}s")

        // Display current question and restart timer
        runOnUiThread {
            displayQuestion()
            startTimer()
        }
    }

    private fun clearSavedQuizState() {
        Log.d(TAG, "clearSavedQuizState: Clearing saved quiz state")
        val editor = sharedPreferences.edit()
        editor.remove(KEY_TIME_LEFT)
        editor.remove(KEY_CURRENT_QUESTION)
        editor.remove(KEY_USER_ANSWERS)
        editor.remove(KEY_QUIZ_LIST)
        editor.remove(KEY_QUIZ_START_TIME)
        editor.remove(KEY_QUIZ_ID)
        editor.remove(KEY_HAS_INTERACTED)
        editor.apply()
    }

    // ═══════════════════════════════════════
    // Dialog Methods
    // ═══════════════════════════════════════

    private fun showRestoreQuizDialog() {
        MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded)
            .setTitle("📝 Resume Quiz")
            .setMessage("You have an unfinished quiz. Would you like to continue where you left off?")
            .setPositiveButton("Resume") { dialog, _ ->
                dialog.dismiss()
                restoreQuizState()
            }
            .setNegativeButton("Start New") { dialog, _ ->
                dialog.dismiss()
                clearSavedQuizState()
                loadQuizQuestions()
            }
            .setCancelable(false)
            .setBackground(ColorDrawable(Color.WHITE))
            .show()
            .apply {
                // Style buttons with brand colors
                getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(getColor(R.color.brand_blue))
                getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(getColor(R.color.brand_teal))
            }
    }

    private fun showQuitConfirmationDialog() {
        MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded)
            .setTitle("⚠️ Quit Quiz")
            .setMessage("Are you sure you want to quit?\n\nYour progress will be saved, but this operation cannot be undone if you choose to start a new quiz later.")
            .setPositiveButton("Quit") { dialog, _ ->
                dialog.dismiss()
                // Only save state if user has interacted with the quiz
                if (hasUserInteracted) {
                    saveQuizState()
                }
                countDownTimer?.cancel()
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .setBackground(ColorDrawable(Color.WHITE))
            .show()
            .apply {
                // Style buttons with brand colors
                getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(getColor(R.color.error_red))
                getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(getColor(R.color.brand_teal))
            }
    }
}
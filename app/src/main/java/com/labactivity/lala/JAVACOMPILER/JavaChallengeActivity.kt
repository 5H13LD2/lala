package com.labactivity.lala.JAVACOMPILER

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.UTILS.DialogUtils
import com.labactivity.lala.R
import com.labactivity.lala.JAVACOMPILER.models.JavaChallenge
import com.labactivity.lala.JAVACOMPILER.services.FirestoreJavaHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.text.Editable
import android.text.TextWatcher

/**
 * Activity for solving Java coding challenges
 * Similar to Python assessment challenges
 */
class JavaChallengeActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var difficultyTextView: TextView
    private lateinit var codeEditText: EditText
    private lateinit var runButton: Button
    private lateinit var hintButton: Button
    private lateinit var submitButton: Button
    private lateinit var outputTextView: TextView
    private lateinit var backButton: ImageButton
    private lateinit var lineNumbersTextView: TextView

    private val javaRunner = JavaRunner()
    private val javaHelper = FirestoreJavaHelper.getInstance()
    private val handler = Handler(Looper.getMainLooper())

    private var challenge: JavaChallenge? = null
    private var challengeId: String = ""
    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_java_challenge)

        // Get challenge ID from intent
        challengeId = intent.getStringExtra("CHALLENGE_ID") ?: ""

        if (challengeId.isEmpty()) {
            Toast.makeText(this, "Error: Challenge ID not provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupListeners()
        loadChallenge()

        // Record start time
        startTime = System.currentTimeMillis()
    }

    private fun initializeViews() {
        titleTextView = findViewById(R.id.titleTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        difficultyTextView = findViewById(R.id.difficultyTextView)
        codeEditText = findViewById(R.id.codeEditText)
        runButton = findViewById(R.id.runButton)
        hintButton = findViewById(R.id.hint)
        submitButton = findViewById(R.id.submitButton)
        outputTextView = findViewById(R.id.outputTextView)
        backButton = findViewById(R.id.backButton)
        lineNumbersTextView = findViewById(R.id.lineNumbersTextView)

        // Enable scrolling for output
        outputTextView.movementMethod = ScrollingMovementMethod()

        // Update line numbers as user types
        codeEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateLineNumbers()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            finish()
        }

        runButton.setOnClickListener {
            runJavaCode()
        }

        hintButton.setOnClickListener {
            showHint()
        }

        submitButton.setOnClickListener {
            submitSolution()
        }
    }

    private fun loadChallenge() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val loadedChallenge = withContext(Dispatchers.IO) {
                    javaHelper.getChallengeById(challengeId)
                }

                if (loadedChallenge == null) {
                    DialogUtils.showErrorDialog(
                        this@JavaChallengeActivity,
                        "Error",
                        "Challenge not found"
                    )
                    finish()
                    return@launch
                }

                challenge = loadedChallenge
                displayChallenge(loadedChallenge)

            } catch (e: Exception) {
                DialogUtils.showErrorDialog(
                    this@JavaChallengeActivity,
                    "Error",
                    "Failed to load challenge: ${e.message}"
                )
                finish()
            }
        }
    }

    private fun displayChallenge(challenge: JavaChallenge) {
        titleTextView.text = challenge.title
        descriptionTextView.text = challenge.description
        difficultyTextView.text = challenge.difficulty

        // Set difficulty color
        val difficultyColor = when (challenge.difficulty.lowercase()) {
            "easy" -> getColor(R.color.success_green)
            "medium" -> getColor(R.color.modern_warning)
            "hard" -> getColor(R.color.error_red)
            else -> getColor(R.color.text_secondary)
        }
        difficultyTextView.setTextColor(difficultyColor)

        // Load broken code into editor
        codeEditText.setText(challenge.brokenCode)
        updateLineNumbers()
    }

    private fun updateLineNumbers() {
        val code = codeEditText.text.toString()
        val lineCount = code.split("\n").size
        val lineNumbers = (1..lineCount).joinToString("\n")
        lineNumbersTextView.text = lineNumbers
    }

    private fun runJavaCode() {
        val userCode = codeEditText.text.toString().trim()

        if (userCode.isBlank()) {
            DialogUtils.showWarningDialog(this, "Empty Code", "Please enter some Java code.")
            return
        }

        // Disable buttons while running
        runButton.isEnabled = false
        submitButton.isEnabled = false
        outputTextView.text = "Compiling and running Java code...\n"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Execute Java code using JavaRunner
                val result = javaRunner.executeJavaCode(
                    javaCode = userCode,
                    className = extractClassName(userCode),
                    methodName = "run"
                )

                withContext(Dispatchers.Main) {
                    if (result.success) {
                        outputTextView.text = "✓ Execution Successful\n\n${result.output}"
                    } else {
                        outputTextView.text = "✗ Execution Failed\n\n${result.error}"
                    }

                    runButton.isEnabled = true
                    submitButton.isEnabled = true
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    DialogUtils.showErrorDialog(
                        this@JavaChallengeActivity,
                        "Execution Error",
                        "Failed to run code: ${e.message}"
                    )
                    outputTextView.text = "Error: ${e.message}"
                    runButton.isEnabled = true
                    submitButton.isEnabled = true
                }
            }
        }
    }

    private fun submitSolution() {
        val currentChallenge = challenge ?: return
        val userCode = codeEditText.text.toString().trim()

        if (userCode.isBlank()) {
            DialogUtils.showWarningDialog(this, "Empty Code", "Please write some code before submitting.")
            return
        }

        // Disable buttons while checking
        runButton.isEnabled = false
        submitButton.isEnabled = false
        outputTextView.text = "Validating your solution...\n"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Execute the user's code
                val result = javaRunner.executeJavaCode(
                    javaCode = userCode,
                    className = extractClassName(userCode),
                    methodName = "run"
                )

                // Check if output matches expected output
                val userOutput = result.output.trim()
                val expectedOutput = currentChallenge.correctOutput.trim()

                val passed = if (result.success) {
                    // Normalize outputs for comparison
                    normalizeOutput(userOutput) == normalizeOutput(expectedOutput)
                } else {
                    false
                }

                // Calculate score
                val score = if (passed) 100 else 0

                // Calculate time taken
                val timeTaken = (System.currentTimeMillis() - startTime) / 1000

                // Save progress to Firestore
                val saveSuccess = javaHelper.updateProgressAfterAttempt(
                    challengeId = challengeId,
                    passed = passed,
                    score = score,
                    userCode = userCode,
                    timeTaken = timeTaken
                )

                withContext(Dispatchers.Main) {
                    if (passed) {
                        showSuccessDialog(score, timeTaken)
                    } else {
                        showFailureDialog(userOutput, expectedOutput)
                    }

                    runButton.isEnabled = true
                    submitButton.isEnabled = true
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    DialogUtils.showErrorDialog(
                        this@JavaChallengeActivity,
                        "Submission Error",
                        "Failed to submit solution: ${e.message}"
                    )
                    runButton.isEnabled = true
                    submitButton.isEnabled = true
                }
            }
        }
    }

    private fun showHint() {
        val currentChallenge = challenge ?: return

        if (currentChallenge.hint.isNotEmpty()) {
            DialogUtils.showInfoDialog(
                this,
                "💡 Hint",
                currentChallenge.hint
            )
        } else {
            Toast.makeText(this, "No hint available for this challenge", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSuccessDialog(score: Int, timeTaken: Long) {
        val message = """
            🎉 Congratulations!

            Your solution is correct!

            Score: $score/100
            Time: ${timeTaken}s
        """.trimIndent()

        DialogUtils.showSuccessDialog(
            this,
            "✓ Challenge Completed",
            message
        ) {
            finish() // Close activity after success
        }
    }

    private fun showFailureDialog(userOutput: String, expectedOutput: String) {
        val message = """
            Your solution didn't produce the expected output.

            Expected:
            $expectedOutput

            Your output:
            $userOutput

            Try again!
        """.trimIndent()

        DialogUtils.showErrorDialog(
            this,
            "✗ Incorrect Solution",
            message
        )
    }

    /**
     * Extract class name from Java code
     */
    private fun extractClassName(code: String): String {
        val regex = "\\bclass\\s+(\\w+)".toRegex()
        val match = regex.find(code)
        return match?.groupValues?.get(1) ?: "Test"
    }

    /**
     * Normalize output for comparison (remove extra whitespace, normalize line endings)
     */
    private fun normalizeOutput(output: String): String {
        return output
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .trim()
            .split("\n")
            .joinToString("\n") { it.trim() }
    }
}

package com.labactivity.lala.UNIFIEDCOMPILER.ui

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.labactivity.lala.R
import com.labactivity.lala.UNIFIEDCOMPILER.CompilerFactory
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerConfig
import com.labactivity.lala.UNIFIEDCOMPILER.services.CompilerService
import kotlinx.coroutines.launch

/**
 * UNIVERSAL COMPILER ACTIVITY
 *
 * This single activity can run code for ANY programming language
 * using the unified compiler system.
 *
 * Features:
 * - Language selector (Python, Java, Kotlin)
 * - Code editor with line numbers (optional)
 * - Run button
 * - Output display
 * - Error handling
 * - Test case results
 * - Execution time tracking
 *
 * Usage:
 * Start this activity with:
 * - EXTRA_LANGUAGE: "python", "java", "kotlin", etc. (optional, defaults to python)
 * - EXTRA_COURSE_ID: Course ID from Firebase (optional, uses language directly if not provided)
 * - EXTRA_INITIAL_CODE: Pre-populated code (optional)
 */
class UnifiedCompilerActivity : AppCompatActivity() {

    // UI Components
    private lateinit var toolbar: MaterialToolbar
    private lateinit var languageChipGroup: ChipGroup
    private lateinit var codeEditor: EditText
    private lateinit var btnRun: MaterialButton
    private lateinit var btnClear: MaterialButton
    private lateinit var tvOutput: TextView
    private lateinit var tvError: TextView
    private lateinit var tvExecutionTime: TextView
    private lateinit var tvTestResults: TextView
    private lateinit var errorCard: MaterialCardView
    private lateinit var testResultsCard: MaterialCardView
    private lateinit var loadingOverlay: View

    // Services
    private val compilerService = CompilerService()

    // State
    private var currentLanguage = "python"
    private var courseId: String? = null

    companion object {
        const val EXTRA_LANGUAGE = "extra_language"
        const val EXTRA_COURSE_ID = "extra_course_id"
        const val EXTRA_INITIAL_CODE = "extra_initial_code"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unified_compiler)

        // Initialize CompilerFactory if not already done
        if (!CompilerFactory.hasCompiler("python")) {
            CompilerFactory.initialize(applicationContext)
        }

        initializeViews()
        setupToolbar()
        setupLanguageSelector()
        setupButtons()
        loadInitialData()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        languageChipGroup = findViewById(R.id.languageChipGroup)
        codeEditor = findViewById(R.id.codeEditor)
        btnRun = findViewById(R.id.btnRun)
        btnClear = findViewById(R.id.btnClear)
        tvOutput = findViewById(R.id.tvOutput)
        tvError = findViewById(R.id.tvError)
        tvExecutionTime = findViewById(R.id.tvExecutionTime)
        tvTestResults = findViewById(R.id.tvTestResults)
        errorCard = findViewById(R.id.errorCard)
        testResultsCard = findViewById(R.id.testResultsCard)
        loadingOverlay = findViewById(R.id.loadingOverlay)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupLanguageSelector() {
        // Get supported languages from CompilerFactory
        val supportedLanguages = CompilerFactory.getSupportedLanguages()

        // Set chip click listeners
        languageChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedChip = findViewById<Chip>(checkedIds[0])
                currentLanguage = when (checkedChip.id) {
                    R.id.chipPython -> "python"
                    R.id.chipJava -> "java"
                    R.id.chipKotlin -> "kotlin"
                    else -> "python"
                }

                updateEditorHint()
                loadSampleCode()
            }
        }

        // Select default language from intent or use python
        val initialLanguage = intent.getStringExtra(EXTRA_LANGUAGE) ?: "python"
        selectLanguageChip(initialLanguage)
    }

    private fun setupButtons() {
        btnRun.setOnClickListener {
            val code = codeEditor.text.toString()
            if (code.isBlank()) {
                Toast.makeText(this, "Please write some code first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            executeCode(code)
        }

        btnClear.setOnClickListener {
            codeEditor.setText("")
            tvOutput.text = "Output will appear here..."
            tvOutput.setTextColor(getColor(android.R.color.holo_green_light))
            errorCard.visibility = View.GONE
            testResultsCard.visibility = View.GONE
            tvExecutionTime.text = "0ms"
        }
    }

    private fun loadInitialData() {
        // Load course ID if provided
        courseId = intent.getStringExtra(EXTRA_COURSE_ID)

        // Load initial code if provided
        val initialCode = intent.getStringExtra(EXTRA_INITIAL_CODE)
        if (!initialCode.isNullOrEmpty()) {
            codeEditor.setText(initialCode)
        } else {
            loadSampleCode()
        }
    }

    private fun selectLanguageChip(language: String) {
        val chipId = when (language.lowercase()) {
            "python" -> R.id.chipPython
            "java" -> R.id.chipJava
            "kotlin" -> R.id.chipKotlin
            else -> R.id.chipPython
        }
        languageChipGroup.check(chipId)
        currentLanguage = language
    }

    private fun updateEditorHint() {
        val hint = when (currentLanguage) {
            "python" -> "# Write Python code here\nprint('Hello, World!')"
            "java" -> "// Write Java code here\npublic class Test {\n    public void run() {\n        System.out.println(\"Hello, World!\");\n    }\n}"
            "kotlin" -> "// Write Kotlin code here\nprintln(\"Hello, World!\")"
            else -> "Write your code here..."
        }
        codeEditor.hint = hint
    }

    private fun loadSampleCode() {
        if (codeEditor.text.toString().isBlank()) {
            val sampleCode = when (currentLanguage) {
                "python" -> """
# Python Example
print("Hello from Python!")

for i in range(5):
    print(f"Number: {i}")
                """.trimIndent()

                "java" -> """
// Java Example
public class Test {
    public void run() {
        System.out.println("Hello from Java!");

        for (int i = 0; i < 5; i++) {
            System.out.println("Number: " + i);
        }
    }
}
                """.trimIndent()

                "kotlin" -> """
// Kotlin Example
println("Hello from Kotlin!")

for (i in 0..4) {
    println("Number: ${'$'}i")
}
                """.trimIndent()

                else -> ""
            }

            if (sampleCode.isNotEmpty()) {
                codeEditor.setText(sampleCode)
            }
        }
    }

    private fun executeCode(code: String) {
        lifecycleScope.launch {
            try {
                showLoading(true)
                errorCard.visibility = View.GONE
                testResultsCard.visibility = View.GONE

                // Execute using either course ID or direct language
                val result = if (courseId != null) {
                    compilerService.executeCodeForCourse(courseId!!, code)
                } else {
                    val compiler = CompilerFactory.getCompiler(currentLanguage)
                    compiler.compile(code, CompilerConfig())
                }

                showLoading(false)

                // Display results
                if (result.success) {
                    tvOutput.text = result.output.ifEmpty { "Execution completed successfully (no output)" }
                    tvOutput.setTextColor(getColor(android.R.color.holo_green_light))
                    tvExecutionTime.text = "${result.executionTime}ms"

                    // Show test results if available
                    if (result.totalTestCases > 0) {
                        showTestResults(result.testCasesPassed, result.totalTestCases)
                    }

                    Toast.makeText(this@UnifiedCompilerActivity, "✓ Execution successful", Toast.LENGTH_SHORT).show()

                } else {
                    tvOutput.text = result.output.ifEmpty { "Execution failed" }
                    tvOutput.setTextColor(getColor(android.R.color.holo_red_light))
                    tvExecutionTime.text = "${result.executionTime}ms"

                    // Show error
                    if (result.error != null) {
                        showError(result.error)
                    }

                    Toast.makeText(this@UnifiedCompilerActivity, "✗ Execution failed", Toast.LENGTH_SHORT).show()
                }

            } catch (e: IllegalArgumentException) {
                showLoading(false)
                val errorMsg = "Compiler not found: ${e.message}"
                showError(errorMsg)
                tvOutput.text = "Error: Language '$currentLanguage' is not supported"
                tvOutput.setTextColor(getColor(android.R.color.holo_red_light))
                Toast.makeText(this@UnifiedCompilerActivity, errorMsg, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                showLoading(false)
                val errorMsg = "Execution Error: ${e.localizedMessage ?: e.message ?: "Unknown error occurred"}"
                showError(errorMsg)
                tvOutput.text = "Fatal error during execution"
                tvOutput.setTextColor(getColor(android.R.color.holo_red_light))
                Toast.makeText(this@UnifiedCompilerActivity, errorMsg, Toast.LENGTH_LONG).show()
                android.util.Log.e("UnifiedCompiler", "Execution failed", e)
            }
        }
    }

    private fun showError(error: String) {
        errorCard.visibility = View.VISIBLE
        tvError.text = error
    }

    private fun showTestResults(passed: Int, total: Int) {
        testResultsCard.visibility = View.VISIBLE
        val percentage = (passed * 100) / total
        tvTestResults.text = """
            ✓ Passed: $passed / $total
            Score: $percentage%
        """.trimIndent()
    }

    private fun showLoading(show: Boolean) {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        btnRun.isEnabled = !show
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up if needed
    }
}

package com.labactivity.lala.UNIFIEDCOMPILER.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ScrollView
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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

/**
 * UNIVERSAL COMPILER ACTIVITY
 *
 * This single activity can run code for ANY programming language
 * using the unified compiler system.
 *
 * Features:
 * - Language selector (Python, Java, Kotlin)
 * - Code editor with synchronized line numbers
 * - Quick input buttons for common characters
 * - Run button
 * - Output display
 * - Error handling
 * - Test case results
 * - Execution time tracking
 * - Cursor position tracking
 * - Character and line count
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
    private lateinit var btnHint: MaterialButton
    private lateinit var tvOutput: TextView
    private lateinit var tvError: TextView
    private lateinit var tvExecutionTime: TextView
    private lateinit var tvTestResults: TextView
    private lateinit var tvTestSummary: TextView
    private lateinit var errorCard: MaterialCardView
    private lateinit var testResultsCard: MaterialCardView
    private lateinit var loadingOverlay: View
    private lateinit var tvLoadingText: TextView

    // Line number components
    private lateinit var tvLineNumbers: TextView
    private lateinit var lineNumberScroll: ScrollView
    private lateinit var codeEditorScroll: ScrollView
    private lateinit var horizontalScrollView: HorizontalScrollView

    // Editor info components
    private lateinit var tvFileName: TextView
    private lateinit var tvCursorPosition: TextView
    private lateinit var tvLanguageIndicator: TextView
    private lateinit var tvCharCount: TextView
    private lateinit var tvLineCount: TextView
    private lateinit var tvErrorLine: TextView

    // Quick input buttons
    private lateinit var btnTab: MaterialButton
    private lateinit var btnBrackets: MaterialButton
    private lateinit var btnParens: MaterialButton
    private lateinit var btnSquare: MaterialButton
    private lateinit var btnQuotes: MaterialButton
    private lateinit var btnSingleQuote: MaterialButton
    private lateinit var btnSemicolon: MaterialButton
    private lateinit var btnColon: MaterialButton
    private lateinit var btnUndo: MaterialButton
    private lateinit var btnRedo: MaterialButton
    private lateinit var btnCopyOutput: ImageButton

    // Services
    private val compilerService = CompilerService()

    // State
    private var currentLanguage = "python"
    private var courseId: String? = null
    private var challengeHints: List<String> = emptyList()
    private var challengeHint: String = ""
    private var challengeDescription: String = ""
    private var currentHintIndex = 0

    // Undo/Redo stacks
    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()
    private var isUndoRedoOperation = false

    companion object {
        const val EXTRA_LANGUAGE = "extra_language"
        const val EXTRA_COURSE_ID = "extra_course_id"
        const val EXTRA_INITIAL_CODE = "extra_initial_code"
        const val EXTRA_CHALLENGE_DESCRIPTION = "extra_challenge_description"
        const val EXTRA_CHALLENGE_HINT = "extra_challenge_hint"
        const val EXTRA_CHALLENGE_HINTS = "extra_challenge_hints"
        private const val MAX_UNDO_HISTORY = 50
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
        setupQuickInputButtons()
        setupCodeEditor()
        setupScrollSync()
        loadInitialData()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        languageChipGroup = findViewById(R.id.languageChipGroup)
        codeEditor = findViewById(R.id.codeEditor)
        btnRun = findViewById(R.id.btnRun)
        btnClear = findViewById(R.id.btnClear)
        btnHint = findViewById(R.id.btnHint)
        tvOutput = findViewById(R.id.tvOutput)
        tvError = findViewById(R.id.tvError)
        tvExecutionTime = findViewById(R.id.tvExecutionTime)
        tvTestResults = findViewById(R.id.tvTestResults)
        tvTestSummary = findViewById(R.id.tvTestSummary)
        errorCard = findViewById(R.id.errorCard)
        testResultsCard = findViewById(R.id.testResultsCard)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        tvLoadingText = findViewById(R.id.tvLoadingText)

        // Line number components
        tvLineNumbers = findViewById(R.id.tvLineNumbers)
        lineNumberScroll = findViewById(R.id.lineNumberScroll)
        codeEditorScroll = findViewById(R.id.codeEditorScroll)
        horizontalScrollView = findViewById(R.id.horizontalScrollView)

        // Editor info components
        tvFileName = findViewById(R.id.tvFileName)
        tvCursorPosition = findViewById(R.id.tvCursorPosition)
        tvLanguageIndicator = findViewById(R.id.tvLanguageIndicator)
        tvCharCount = findViewById(R.id.tvCharCount)
        tvLineCount = findViewById(R.id.tvLineCount)
        tvErrorLine = findViewById(R.id.tvErrorLine)

        // Quick input buttons
        btnTab = findViewById(R.id.btnTab)
        btnBrackets = findViewById(R.id.btnBrackets)
        btnParens = findViewById(R.id.btnParens)
        btnSquare = findViewById(R.id.btnSquare)
        btnQuotes = findViewById(R.id.btnQuotes)
        btnSingleQuote = findViewById(R.id.btnSingleQuote)
        btnSemicolon = findViewById(R.id.btnSemicolon)
        btnColon = findViewById(R.id.btnColon)
        btnUndo = findViewById(R.id.btnUndo)
        btnRedo = findViewById(R.id.btnRedo)
        btnCopyOutput = findViewById(R.id.btnCopyOutput)
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

                updateEditorForLanguage()
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
            tvOutput.text = "$ Ready to execute...\n"
            tvOutput.setTextColor(getColor(android.R.color.holo_green_light))
            errorCard.visibility = View.GONE
            testResultsCard.visibility = View.GONE
            tvExecutionTime.visibility = View.GONE
            undoStack.clear()
            redoStack.clear()
            updateLineNumbers()
            updateEditorStats()
        }

        btnHint.setOnClickListener {
            showHint()
        }

        btnCopyOutput.setOnClickListener {
            val output = tvOutput.text.toString()
            if (output.isNotBlank()) {
                copyToClipboard(output)
                Toast.makeText(this, "Output copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupQuickInputButtons() {
        btnTab.setOnClickListener { insertTextAtCursor("    ") } // 4 spaces for tab
        btnBrackets.setOnClickListener { insertWrappingChars("{", "}") }
        btnParens.setOnClickListener { insertWrappingChars("(", ")") }
        btnSquare.setOnClickListener { insertWrappingChars("[", "]") }
        btnQuotes.setOnClickListener { insertWrappingChars("\"", "\"") }
        btnSingleQuote.setOnClickListener { insertWrappingChars("'", "'") }
        btnSemicolon.setOnClickListener { insertTextAtCursor(";") }
        btnColon.setOnClickListener { insertTextAtCursor(":") }

        btnUndo.setOnClickListener { performUndo() }
        btnRedo.setOnClickListener { performRedo() }
    }

    private fun setupCodeEditor() {
        // Add text change listener for line numbers and stats
        codeEditor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Save state for undo before change (if not an undo/redo operation)
                if (!isUndoRedoOperation && s != null) {
                    saveToUndoStack(s.toString())
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Update line numbers as user types
                updateLineNumbers()
            }

            override fun afterTextChanged(s: Editable?) {
                // Update character count and line count
                updateEditorStats()
                // Clear redo stack on new input (if not an undo/redo operation)
                if (!isUndoRedoOperation) {
                    redoStack.clear()
                }
            }
        })

        // Track cursor position
        codeEditor.setOnClickListener {
            updateCursorPosition()
        }

        codeEditor.accessibilityDelegate = object : View.AccessibilityDelegate() {}

        // Also update on selection change
        codeEditor.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                updateCursorPosition()
            }
        }

        // Initial line numbers
        updateLineNumbers()
        updateEditorStats()
    }

    private fun setupScrollSync() {
        // Sync vertical scroll between line numbers and code editor
        codeEditorScroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            lineNumberScroll.scrollTo(0, scrollY)
        }

        lineNumberScroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            codeEditorScroll.scrollTo(0, scrollY)
        }
    }

    private fun updateLineNumbers() {
        val text = codeEditor.text.toString()
        val lineCount = if (text.isEmpty()) 1 else text.lines().size

        val lineNumbers = StringBuilder()
        for (i in 1..lineCount) {
            lineNumbers.append(i)
            if (i < lineCount) {
                lineNumbers.append("\n")
            }
        }

        tvLineNumbers.text = lineNumbers.toString()
    }

    private fun updateEditorStats() {
        val text = codeEditor.text.toString()
        val charCount = text.length
        val lineCount = if (text.isEmpty()) 1 else text.lines().size

        tvCharCount.text = "$charCount chars"
        tvLineCount.text = if (lineCount == 1) "1 line" else "$lineCount lines"
    }

    private fun updateCursorPosition() {
        val selectionStart = codeEditor.selectionStart
        val text = codeEditor.text.toString()

        if (selectionStart >= 0 && selectionStart <= text.length) {
            val textBeforeCursor = text.substring(0, selectionStart)
            val lineNumber = textBeforeCursor.count { it == '\n' } + 1
            val lastNewlineIndex = textBeforeCursor.lastIndexOf('\n')
            val columnNumber = if (lastNewlineIndex == -1) {
                selectionStart + 1
            } else {
                selectionStart - lastNewlineIndex
            }

            tvCursorPosition.text = "Ln $lineNumber, Col $columnNumber"
        }
    }

    private fun updateEditorForLanguage() {
        // Update file name display
        val fileName = when (currentLanguage) {
            "python" -> "main.py"
            "java" -> "Main.java"
            "kotlin" -> "Main.kt"
            else -> "main.txt"
        }
        tvFileName.text = fileName

        // Update language indicator
        tvLanguageIndicator.text = currentLanguage.replaceFirstChar { it.uppercase() }

        // Update language indicator color
        val indicatorColor = when (currentLanguage) {
            "python" -> "#3776AB"
            "java" -> "#F89820"
            "kotlin" -> "#7F52FF"
            else -> "#4CAF50"
        }
        tvLanguageIndicator.setTextColor(android.graphics.Color.parseColor(indicatorColor))

        // Update editor hint
        updateEditorHint()
    }

    private fun insertTextAtCursor(text: String) {
        val start = codeEditor.selectionStart
        val end = codeEditor.selectionEnd
        codeEditor.text.replace(start, end, text)
        codeEditor.setSelection(start + text.length)
    }

    private fun insertWrappingChars(openChar: String, closeChar: String) {
        val start = codeEditor.selectionStart
        val end = codeEditor.selectionEnd

        if (start != end) {
            // Wrap selected text
            val selectedText = codeEditor.text.substring(start, end)
            codeEditor.text.replace(start, end, "$openChar$selectedText$closeChar")
            codeEditor.setSelection(start + 1, end + 1)
        } else {
            // Insert both chars and place cursor between them
            codeEditor.text.insert(start, "$openChar$closeChar")
            codeEditor.setSelection(start + 1)
        }
    }

    private fun saveToUndoStack(text: String) {
        if (undoStack.isEmpty() || undoStack.last() != text) {
            undoStack.add(text)
            // Limit undo history
            if (undoStack.size > MAX_UNDO_HISTORY) {
                undoStack.removeAt(0)
            }
        }
    }

    private fun performUndo() {
        if (undoStack.isNotEmpty()) {
            isUndoRedoOperation = true
            val currentText = codeEditor.text.toString()
            redoStack.add(currentText)

            val previousText = undoStack.removeAt(undoStack.lastIndex)
            codeEditor.setText(previousText)
            codeEditor.setSelection(previousText.length)
            isUndoRedoOperation = false
        } else {
            Toast.makeText(this, "Nothing to undo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performRedo() {
        if (redoStack.isNotEmpty()) {
            isUndoRedoOperation = true
            val currentText = codeEditor.text.toString()
            undoStack.add(currentText)

            val nextText = redoStack.removeAt(redoStack.lastIndex)
            codeEditor.setText(nextText)
            codeEditor.setSelection(nextText.length)
            isUndoRedoOperation = false
        } else {
            Toast.makeText(this, "Nothing to redo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Output", text)
        clipboard.setPrimaryClip(clip)
    }

    /**
     * Show hint dialog with description and hints
     */
    private fun showHint() {
        val dialogBuilder = android.app.AlertDialog.Builder(this)
        dialogBuilder.setTitle("💡 Challenge Hint")

        // Build hint message
        val hintMessage = buildString {
            if (challengeDescription.isNotEmpty()) {
                append("📝 Description:\n")
                append(challengeDescription)
                append("\n\n")
            }

            if (challengeHint.isNotEmpty()) {
                append("💡 Hint:\n")
                append(challengeHint)
                append("\n\n")
            }

            if (challengeHints.isNotEmpty()) {
                if (currentHintIndex < challengeHints.size) {
                    append("💡 Hint ${currentHintIndex + 1}/${challengeHints.size}:\n")
                    append(challengeHints[currentHintIndex])

                    if (currentHintIndex < challengeHints.size - 1) {
                        append("\n\n(Tap 'Next Hint' for more)")
                    }
                } else {
                    append("✓ All hints revealed!")
                }
            }
        }

        dialogBuilder.setMessage(hintMessage)

        // Add buttons based on available hints
        if (challengeHints.isNotEmpty() && currentHintIndex < challengeHints.size - 1) {
            dialogBuilder.setPositiveButton("Next Hint") { dialog, _ ->
                currentHintIndex++
                dialog.dismiss()
                showHint() // Show next hint
            }
            dialogBuilder.setNegativeButton("Close", null)
        } else {
            dialogBuilder.setPositiveButton("Got it!", null)
        }

        dialogBuilder.create().show()
    }

    private fun loadInitialData() {
        // Load course ID if provided
        courseId = intent.getStringExtra(EXTRA_COURSE_ID)

        // Load language - with fallback logic
        val intentLanguage = intent.getStringExtra(EXTRA_LANGUAGE)

        // Determine language from multiple sources
        currentLanguage = when {
            // 1. Use intent language if valid
            !intentLanguage.isNullOrBlank() && isValidLanguage(intentLanguage) -> intentLanguage.lowercase()

            // 2. Try to derive from courseId
            courseId != null -> deriveLanguageFromCourseId(courseId!!)

            // 3. Default to python
            else -> "python"
        }

        // Select the chip for the determined language
        selectLanguageChip(currentLanguage)

        // Load challenge hints and description
        challengeDescription = intent.getStringExtra(EXTRA_CHALLENGE_DESCRIPTION) ?: ""
        challengeHint = intent.getStringExtra(EXTRA_CHALLENGE_HINT) ?: ""
        challengeHints = intent.getStringArrayListExtra(EXTRA_CHALLENGE_HINTS) ?: emptyList()

        val hasHintData = challengeDescription.isNotEmpty() ||
                challengeHint.isNotEmpty() ||
                challengeHints.isNotEmpty()
        btnHint.visibility = if (hasHintData) View.VISIBLE else View.GONE

        // Load initial code if provided
        val initialCode = intent.getStringExtra(EXTRA_INITIAL_CODE)
        if (!initialCode.isNullOrEmpty()) {
            codeEditor.setText(initialCode)
        } else {
            loadSampleCode()
        }

        // Update UI for initial language
        updateEditorForLanguage()
    }

    /**
     * Check if a language string is valid
     */
    private fun isValidLanguage(language: String): Boolean {
        val validLanguages = listOf("python", "python3", "py", "java", "kotlin", "kt")
        return language.lowercase().trim() in validLanguages
    }

    /**
     * Derive language from courseId naming convention
     */
    private fun deriveLanguageFromCourseId(courseId: String): String {
        return when {
            courseId.contains("python", ignoreCase = true) -> "python"
            courseId.contains("java", ignoreCase = true) -> "java"
            courseId.contains("kotlin", ignoreCase = true) -> "kotlin"
            courseId.contains("py", ignoreCase = true) -> "python"
            courseId.contains("kt", ignoreCase = true) -> "kotlin"
            else -> "python" // Safe default
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
                showLoading(true, "Compiling...")
                errorCard.visibility = View.GONE
                testResultsCard.visibility = View.GONE

                // Debug logging
                android.util.Log.d("UnifiedCompiler", "courseId: $courseId")
                android.util.Log.d("UnifiedCompiler", "currentLanguage: $currentLanguage")

                // FIXED: Always use currentLanguage (which has fallback logic)
                val compiler = CompilerFactory.getCompiler(currentLanguage)
                val result = compiler.compile(code, CompilerConfig())

                showLoading(false)

                // Display results
                if (result.success) {
                    tvOutput.text = if (result.output.isNotEmpty()) {
                        "$ ${result.output}"
                    } else {
                        "$ Execution completed successfully (no output)"
                    }
                    tvOutput.setTextColor(getColor(android.R.color.holo_green_light))
                    tvExecutionTime.text = "⏱ ${result.executionTime}ms"
                    tvExecutionTime.visibility = View.VISIBLE

                    // Show test results if available
                    if (result.totalTestCases > 0) {
                        showTestResults(result.testCasesPassed, result.totalTestCases)
                    }

                    Toast.makeText(this@UnifiedCompilerActivity, "✓ Execution successful", Toast.LENGTH_SHORT).show()

                } else {
                    tvOutput.text = if (result.output.isNotEmpty()) {
                        "$ ${result.output}"
                    } else {
                        "$ Execution failed"
                    }
                    tvOutput.setTextColor(getColor(android.R.color.holo_red_light))
                    tvExecutionTime.text = "⏱ ${result.executionTime}ms"
                    tvExecutionTime.visibility = View.VISIBLE

                    // Show error
                    if (result.error != null) {
                        showError(result.error, parseErrorLine(result.error))
                    }

                    Toast.makeText(this@UnifiedCompilerActivity, "✗ Execution failed", Toast.LENGTH_SHORT).show()
                }

            } catch (e: IllegalArgumentException) {
                showLoading(false)
                val errorMsg = "Compiler not found: ${e.message}"
                showError(errorMsg, null)
                tvOutput.text = "$ Error: Language '$currentLanguage' is not supported"
                tvOutput.setTextColor(getColor(android.R.color.holo_red_light))
                Toast.makeText(this@UnifiedCompilerActivity, errorMsg, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                showLoading(false)
                val errorMsg = "Execution Error: ${e.localizedMessage ?: e.message ?: "Unknown error occurred"}"
                showError(errorMsg, null)
                tvOutput.text = "$ Fatal error during execution"
                tvOutput.setTextColor(getColor(android.R.color.holo_red_light))
                Toast.makeText(this@UnifiedCompilerActivity, errorMsg, Toast.LENGTH_LONG).show()
                android.util.Log.e("UnifiedCompiler", "Execution failed", e)
            }
        }
    }

    private fun parseErrorLine(error: String): Int? {
        // Try to extract line number from common error formats
        val patterns = listOf(
            Regex("line (\\d+)"),
            Regex("Line (\\d+)"),
            Regex(":(\\d+):"),
            Regex("at line (\\d+)")
        )

        for (pattern in patterns) {
            val match = pattern.find(error)
            if (match != null) {
                return match.groupValues[1].toIntOrNull()
            }
        }
        return null
    }

    private fun showError(error: String, lineNumber: Int?) {
        errorCard.visibility = View.VISIBLE
        tvError.text = error

        if (lineNumber != null) {
            tvErrorLine.visibility = View.VISIBLE
            tvErrorLine.text = "Line $lineNumber"
        } else {
            tvErrorLine.visibility = View.GONE
        }
    }

    private fun showTestResults(passed: Int, total: Int) {
        testResultsCard.visibility = View.VISIBLE
        tvTestSummary.text = "$passed/$total passed"

        val percentage = (passed * 100) / total
        tvTestResults.text = buildString {
            for (i in 1..total) {
                if (i <= passed) {
                    append("✓ Test $i: Passed\n")
                } else {
                    append("✗ Test $i: Failed\n")
                }
            }
            append("\nScore: $percentage%")
        }
    }

    private fun showLoading(show: Boolean, message: String = "Compiling...") {
        loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        tvLoadingText.text = message
        btnRun.isEnabled = !show
        if (btnHint.visibility == View.VISIBLE) {
            btnHint.isEnabled = !show
        }
        btnClear.isEnabled = !show
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up undo/redo stacks
        undoStack.clear()
        redoStack.clear()
    }
}
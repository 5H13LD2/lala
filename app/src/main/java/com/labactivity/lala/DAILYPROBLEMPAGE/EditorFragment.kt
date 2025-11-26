package com.labactivity.lala.DAILYPROBLEMPAGE

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.labactivity.lala.R
import com.labactivity.lala.databinding.FragmentEditorBinding
import com.labactivity.lala.UNIFIEDCOMPILER.CompilerFactory
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fragment with code editor, run & submit buttons
 */
class EditorFragment : Fragment() {

    private var _binding: FragmentEditorBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: DailyProblemViewModel

    private var problemId: String = ""
    private var courseId: String = ""
    private var compilerType: String = ""
    private var currentProblem: DailyProblem? = null

    private var isCodeView = true // Toggle between code and output

    companion object {
        private const val ARG_PROBLEM_ID = "problem_id"
        private const val ARG_COURSE_ID = "course_id"
        private const val ARG_COMPILER_TYPE = "compiler_type"

        fun newInstance(problemId: String, courseId: String, compilerType: String): EditorFragment {
            return EditorFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROBLEM_ID, problemId)
                    putString(ARG_COURSE_ID, courseId)
                    putString(ARG_COMPILER_TYPE, compilerType)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            problemId = it.getString(ARG_PROBLEM_ID) ?: ""
            courseId = it.getString(ARG_COURSE_ID) ?: ""
            compilerType = it.getString(ARG_COMPILER_TYPE) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = DailyProblemViewModel()

        setupEditor()
        setupToggle()
        setupButtons()
        loadProblemData()
    }

    private fun loadProblemData() {
        // Load the specific problem by ID
        viewModel.loadProblemById(problemId) { problem ->
            currentProblem = problem
            if (problem == null) {
                Toast.makeText(context, "Failed to load problem data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupEditor() {
        // Map old compiler types to new unified types
        val normalizedType = when (compilerType.lowercase()) {
            "javacompiler" -> "java"
            "pythoncompiler" -> "python"
            "sqlcompiler" -> "sql"
            else -> compilerType.lowercase()
        }

        // Set placeholder based on compiler type
        val placeholder = when (normalizedType) {
            "java" -> "// Write your Java code here\npublic class Solution {\n    public void run() {\n        // Your code\n    }\n}"
            "python" -> "# Write your Python code here\ndef solve():\n    # Your code\n    pass"
            "sql" -> "-- Write your SQL query here\nSELECT * FROM table_name;"
            "kotlin" -> "// Write your Kotlin code here\nfun solve() {\n    // Your code\n}"
            else -> "// Write your code here"
        }
        binding.etCodeEditor.hint = placeholder
    }

    private fun setupToggle() {
        // Toggle between Code and Output views
        binding.btnCodeTab.setOnClickListener {
            showCodeView()
        }

        binding.btnOutputTab.setOnClickListener {
            showOutputView()
        }
    }

    private fun showCodeView() {
        isCodeView = true
        binding.editorCard.isVisible = true
        binding.outputCard.isVisible = false

        // Update tab styling
        binding.btnCodeTab.setBackgroundResource(R.drawable.modern_button_primary)
        binding.btnCodeTab.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))

        binding.btnOutputTab.setBackgroundColor(android.graphics.Color.parseColor("#252526"))
        binding.btnOutputTab.setTextColor(android.graphics.Color.parseColor("#AAAAAA"))
    }

    private fun showOutputView() {
        isCodeView = false
        binding.editorCard.isVisible = false
        binding.outputCard.isVisible = true

        // Update tab styling
        binding.btnOutputTab.setBackgroundResource(R.drawable.modern_button_primary)
        binding.btnOutputTab.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))

        binding.btnCodeTab.setBackgroundColor(android.graphics.Color.parseColor("#252526"))
        binding.btnCodeTab.setTextColor(android.graphics.Color.parseColor("#AAAAAA"))
    }

    private fun setupButtons() {
        // Run button - execute code without saving
        binding.btnRun.setOnClickListener {
            runCode()
        }

        // Submit button - validate and save submission
        binding.btnSubmit.setOnClickListener {
            submitCode()
        }
    }

    private fun runCode() {
        val code = binding.etCodeEditor.text.toString().trim()

        if (code.isEmpty()) {
            Toast.makeText(context, "Please write some code first", Toast.LENGTH_SHORT).show()
            return
        }

        showOutputView()
        binding.tvOutput.text = "Running code...\n"

        lifecycleScope.launch {
            val output = executeCode(code, isTest = true)
            binding.tvOutput.text = output
        }
    }

    private fun submitCode() {
        val code = binding.etCodeEditor.text.toString().trim()

        if (code.isEmpty()) {
            Toast.makeText(context, "Please write some code first", Toast.LENGTH_SHORT).show()
            return
        }

        showOutputView()
        binding.tvOutput.text = "Validating submission...\n"

        lifecycleScope.launch {
            val result = validateSubmission(code)

            if (result.passed) {
                binding.tvOutput.text = "✓ All test cases passed!\n\nScore: ${result.score}\nTime: ${result.executionTime}ms"
                binding.tvOutput.setTextColor(android.graphics.Color.parseColor("#4CAF50"))

                // Save to Firestore
                viewModel.submitSolution(
                    problemId = problemId,
                    courseId = courseId,
                    code = code,
                    status = "completed",
                    score = result.score,
                    executionTime = result.executionTime,
                    testCasesPassed = result.testCasesPassed,
                    totalTestCases = result.totalTestCases
                )

                Toast.makeText(context, "Congratulations! Problem solved!", Toast.LENGTH_LONG).show()
            } else {
                binding.tvOutput.text = "✗ Test cases failed\n\n${result.output}"
                binding.tvOutput.setTextColor(android.graphics.Color.parseColor("#F44336"))

                // Save failed attempt
                viewModel.submitSolution(
                    problemId = problemId,
                    courseId = courseId,
                    code = code,
                    status = "failed",
                    score = result.score,
                    executionTime = result.executionTime,
                    testCasesPassed = result.testCasesPassed,
                    totalTestCases = result.totalTestCases
                )
            }
        }
    }

    private suspend fun executeCode(code: String, isTest: Boolean): String {
        return withContext(Dispatchers.IO) {
            try {
                // Normalize compiler type
                val normalizedType = when (compilerType.lowercase()) {
                    "javacompiler" -> "java"
                    "pythoncompiler" -> "python"
                    "sqlcompiler" -> "sql"
                    else -> compilerType.lowercase()
                }

                // Get the appropriate compiler from unified system
                val compiler = CompilerFactory.getCompiler(normalizedType)

                // Execute code
                val config = CompilerConfig(
                    timeout = 30000,
                    maxOutputLength = 10000
                )

                val result = compiler.compile(code, config)

                // Return output or error
                if (result.success) {
                    result.output.ifEmpty { "Execution completed successfully (no output)" }
                } else {
                    "Error: ${result.error ?: "Unknown error"}"
                }

            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
    }

    private data class ValidationResult(
        val passed: Boolean,
        val score: Int,
        val executionTime: Long,
        val testCasesPassed: Int,
        val totalTestCases: Int,
        val output: String
    )

    private suspend fun validateSubmission(code: String): ValidationResult {
        return withContext(Dispatchers.IO) {
            try {
                // Normalize compiler type
                val normalizedType = when (compilerType.lowercase()) {
                    "javacompiler" -> "java"
                    "pythoncompiler" -> "python"
                    "sqlcompiler" -> "sql"
                    else -> compilerType.lowercase()
                }

                // Get compiler from unified system
                val compiler = CompilerFactory.getCompiler(normalizedType)

                // Convert DailyProblem TestCases to CompilerConfig TestCases
                val problem = currentProblem
                val testCases = problem?.testCases?.map { tc ->
                    com.labactivity.lala.UNIFIEDCOMPILER.models.TestCase(
                        input = tc.input,
                        expectedOutput = tc.expectedOutput
                    )
                } ?: emptyList()

                // Build config with test cases
                val config = CompilerConfig(
                    timeout = 30000,
                    maxOutputLength = 10000,
                    testCases = testCases
                )

                val result = compiler.compile(code, config)

                // Calculate score based on test cases passed
                val totalTests = testCases.size
                val passedTests = result.testCasesPassed
                val score = if (totalTests > 0) {
                    (passedTests * 100) / totalTests
                } else {
                    // If no test cases, just check if it executed successfully
                    if (result.success) 100 else 0
                }

                val passed = if (totalTests > 0) {
                    passedTests == totalTests
                } else {
                    result.success
                }

                // Build detailed output message
                val outputMessage = buildString {
                    if (totalTests > 0) {
                        append("Test Results:\n")
                        append("✓ Passed: $passedTests / $totalTests\n")
                        append("Score: $score%\n\n")

                        if (passed) {
                            append("🎉 All test cases passed!\n")
                        } else {
                            append("❌ Some test cases failed\n")
                            append("\nYour Output:\n${result.output}\n")
                            if (result.error != null) {
                                append("\nError: ${result.error}")
                            }
                        }
                    } else {
                        // No test cases - just show execution result
                        if (result.success) {
                            append("Execution successful!\n\n")
                            append("Output:\n${result.output}")
                        } else {
                            append("Execution failed\n\n")
                            append("Error: ${result.error ?: "Unknown error"}")
                        }
                    }
                }

                ValidationResult(
                    passed = passed,
                    score = score,
                    executionTime = result.executionTime,
                    testCasesPassed = passedTests,
                    totalTestCases = totalTests,
                    output = outputMessage
                )

            } catch (e: Exception) {
                ValidationResult(
                    passed = false,
                    score = 0,
                    executionTime = 0,
                    testCasesPassed = 0,
                    totalTestCases = 0,
                    output = "Error: ${e.message}"
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

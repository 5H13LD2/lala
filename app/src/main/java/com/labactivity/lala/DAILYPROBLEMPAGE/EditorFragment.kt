package com.labactivity.lala.DAILYPROBLEMPAGE

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.labactivity.lala.databinding.FragmentEditorBinding
import com.labactivity.lala.JAVACOMPILER.JavaRunner
import com.labactivity.lala.SQLCOMPILER.QueryValidator
import com.labactivity.lala.SQLCOMPILER.DatabaseHelper
import com.labactivity.lala.SQLCOMPILER.QueryEvaluator
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
    }

    private fun setupEditor() {
        // Set placeholder based on compiler type
        val placeholder = when (compilerType.lowercase()) {
            "javacompiler" -> "// Write your Java code here\npublic class Solution {\n    public void run() {\n        // Your code\n    }\n}"
            "pythoncompiler" -> "# Write your Python code here\ndef solve():\n    # Your code\n    pass"
            "sqlcompiler" -> "-- Write your SQL query here\nSELECT * FROM table_name;"
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
        binding.etCodeEditor.isVisible = true
        binding.svOutput.isVisible = false

        binding.btnCodeTab.setBackgroundResource(android.R.drawable.btn_default)
        binding.btnOutputTab.setBackgroundResource(android.R.color.transparent)
    }

    private fun showOutputView() {
        isCodeView = false
        binding.etCodeEditor.isVisible = false
        binding.svOutput.isVisible = true

        binding.btnCodeTab.setBackgroundResource(android.R.color.transparent)
        binding.btnOutputTab.setBackgroundResource(android.R.drawable.btn_default)
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
                when (compilerType.lowercase()) {
                    "javacompiler" -> executeJava(code)
                    "pythoncompiler" -> executePython(code)
                    "sqlcompiler" -> executeSQL(code)
                    else -> "Unsupported compiler type: $compilerType"
                }
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
    }

    private fun executeJava(code: String): String {
        val javaRunner = JavaRunner()
        val className = extractJavaClassName(code)
        val result = javaRunner.executeJavaCode(code, className, "run")
        return if (result.success) (result.output ?: "") else (result.error ?: "Unknown error")
    }

    private fun executePython(code: String): String {
        // TODO: Implement Python execution using Chaquopy
        return "Python execution not yet implemented"
    }

    private fun executeSQL(code: String): String {
        // TODO: Implement SQL execution
        return "SQL execution not yet implemented"
    }

    private fun extractJavaClassName(code: String): String {
        val regex = "class\\s+(\\w+)".toRegex()
        return regex.find(code)?.groupValues?.get(1) ?: "Solution"
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
            // TODO: Implement proper validation against test cases
            val startTime = System.currentTimeMillis()
            val output = executeCode(code, isTest = false)
            val executionTime = System.currentTimeMillis() - startTime

            // Placeholder validation
            ValidationResult(
                passed = true,
                score = 100,
                executionTime = executionTime,
                testCasesPassed = 3,
                totalTestCases = 3,
                output = output
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

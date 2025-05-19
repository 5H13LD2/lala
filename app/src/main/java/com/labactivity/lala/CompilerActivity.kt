package com.labactivity.lala

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CompilerActivity : AppCompatActivity() {

    private lateinit var codeEditText: EditText
    private lateinit var lineNumbersTextView: TextView
    private lateinit var codeScrollView: NestedScrollView
    private lateinit var lineNumbersScrollView: NestedScrollView
    private lateinit var runButton: Button
    private lateinit var outputTextView: TextView
    private lateinit var inputContainer: LinearLayout
    private lateinit var userInputEditText: EditText
    private lateinit var submitInputButton: Button
    private lateinit var hintButton: Button
    private val handler = Handler(Looper.getMainLooper())
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private var pythonExecutionActive = false
    private var checkInputTimer: Runnable? = null
    private var outputFile: File? = null

    private var challengeTitle: String = ""
    private var correctOutput: String = ""
    private var hintText: String = ""

    private var pyInputBuffer = ""
    private var waitingForInput = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compiler_line_numbered)

        // Initialize Python if not already started
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        challengeTitle = intent.getStringExtra("CHALLENGE_TITLE") ?: "Debug Code"
        val challengeCode = intent.getStringExtra("CHALLENGE_CODE") ?: ""
        correctOutput = intent.getStringExtra("CORRECT_OUTPUT") ?: ""
        hintText = intent.getStringExtra("HINT") ?: "Try to find the bug in the code."

        // Find UI elements
        codeEditText = findViewById(R.id.codeEditText)
        lineNumbersTextView = findViewById(R.id.lineNumbersTextView)
        codeScrollView = findViewById(R.id.codeScrollView)
        lineNumbersScrollView = findViewById(R.id.lineNumbersScrollView)
        runButton = findViewById(R.id.runButton)
        outputTextView = findViewById(R.id.outputTextView)
        inputContainer = findViewById(R.id.inputContainer)
        userInputEditText = findViewById(R.id.userInputEditText)
        submitInputButton = findViewById(R.id.submitInputButton)
        hintButton = findViewById(R.id.hint)

        // Set the title TextView
        val titleTextView = findViewById<TextView>(R.id.titleTextView)
        titleTextView.text = challengeTitle

        // Enable scrolling for output
        outputTextView.movementMethod = ScrollingMovementMethod()

        // Initialize line numbers
        updateLineNumbers(challengeCode)

        // Set up synchronous scrolling between line numbers and code
        setupSynchronousScrolling()

        // Set text change listener to update line numbers
        codeEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateLineNumbers(s.toString())
            }
        })

        codeEditText.setText(challengeCode)

        // Set click listener for Run button
        runButton.setOnClickListener {
            executePythonScript()
        }

        // Set click listener for Submit Input button
        submitInputButton.setOnClickListener {
            val input = userInputEditText.text.toString()
            submitUserInput(input)
            userInputEditText.text.clear()
            inputContainer.visibility = View.GONE
            waitingForInput = false
        }

        // Set click listener for Hint button
        hintButton.setOnClickListener {
            Toast.makeText(this, hintText, Toast.LENGTH_LONG).show()
        }

        // Define the output file path
        val fileDir = applicationContext.filesDir
        outputFile = File(fileDir, "code_output.txt")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopInputCheck()
        executor.shutdown()
    }

    private fun setupSynchronousScrolling() {
        // Sync line numbers scroll with code scroll
        codeScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            lineNumbersScrollView.scrollTo(0, scrollY)
        }
    }

    private fun updateLineNumbers(text: String) {
        val lines = text.split("\n")
        val lineNumbers = StringBuilder()

        for (i in 1..lines.size) {
            lineNumbers.append("$i\n")
        }

        lineNumbersTextView.text = lineNumbers.toString()
    }

    private fun executePythonScript() {
        val userCode = codeEditText.text.toString().trim()

        if (userCode.isBlank()) {
            Toast.makeText(this, "Please enter some Python code.", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable the run button while execution is in progress
        runButton.isEnabled = false
        outputTextView.text = "Starting execution...\n"
        inputContainer.visibility = View.GONE

        // Set flag to indicate Python execution is active
        pythonExecutionActive = true

        executor.execute {
            try {
                // Start Python environment
                val py = Python.getInstance()
                val pyModule = py.getModule("myscript")

                handler.post {
                    outputTextView.append("Python module loaded, preparing execution...\n")
                }

                // Execute the user code (this should no longer block)
                val result = pyModule.callAttr("execute_code", userCode)

                handler.post {
                    outputTextView.append("Code execution initiated...\n")
                    outputTextView.append("Output file path: ${outputFile?.absolutePath}\n")
                }

                // Start checking for input requests
                startInputCheck(pyModule)

                // Begin checking for output updates
                var checkCount = 0
                while (pythonExecutionActive && checkCount < 100) {  // Limit to prevent infinite loops
                    updateOutput()
                    Thread.sleep(500)  // Check for updates every 500ms

                    // Check if execution has completed
                    try {
                        val isActive = pyModule.callAttr("is_execution_active").toBoolean()
                        if (!isActive) {
                            pythonExecutionActive = false
                            handler.post {
                                outputTextView.append("Python execution completed.\n")

                                // Check if output matches expected output (for challenges)
                                if (correctOutput.isNotEmpty()) {
                                    checkChallengeCompletion()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        handler.post {
                            outputTextView.append("Error checking execution status: ${e.message}\n")
                        }
                    }

                    checkCount++
                }

                // Final output update
                updateOutput()

                handler.post {
                    runButton.isEnabled = true
                    if (checkCount >= 100) {
                        outputTextView.append("Monitoring timed out. Check if script is running too long.\n")
                    }
                }

            } catch (e: Exception) {
                handler.post {
                    Toast.makeText(this, "Execution failed: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                    outputTextView.text =
                        "Error: ${e.message}\n\nStack trace: ${e.stackTraceToString()}"
                    runButton.isEnabled = true
                    pythonExecutionActive = false
                }
                e.printStackTrace()
            }
        }
    }

    // Improved input handling
    private fun startInputCheck(pyModule: com.chaquo.python.PyObject) {
        checkInputTimer = object : Runnable {
            override fun run() {
                if (!pythonExecutionActive) return

                try {
                    val isInputRequested = pyModule.callAttr("is_input_requested").toBoolean()

                    if (isInputRequested) {
                        val prompt = pyModule.callAttr("get_input_prompt").toString()
                        waitingForInput = true

                        handler.post {
                            inputContainer.visibility = View.VISIBLE
                            userInputEditText.hint = prompt.ifEmpty { "Enter input..." }
                            // Request focus and show keyboard
                            userInputEditText.requestFocus()
                        }
                    } else {
                        handler.post {
                            if (inputContainer.visibility == View.VISIBLE && !waitingForInput) {
                                inputContainer.visibility = View.GONE
                            }
                        }
                    }

                    // Continue checking only if execution is still active
                    if (pythonExecutionActive) {
                        handler.postDelayed(this, 300)
                    }
                } catch (e: Exception) {
                    handler.post {
                        outputTextView.append("Input check error: ${e.message}\n")
                        inputContainer.visibility = View.GONE
                    }
                    pythonExecutionActive = false
                }
            }
        }

        handler.post(checkInputTimer!!)
    }

    private fun stopInputCheck() {
        checkInputTimer?.let {
            handler.removeCallbacks(it)
        }
        checkInputTimer = null
        pythonExecutionActive = false
    }

    private fun submitUserInput(input: String) {
        executor.execute {
            try {
                val py = Python.getInstance()
                val pyModule = py.getModule("myscript")

                // Provide input to Python
                pyModule.callAttr("provide_input", input)

                // Hide input container
                handler.post {
                    inputContainer.visibility = View.GONE
                }

                // Update output to show the input
                updateOutput()

            } catch (e: Exception) {
                handler.post {
                    Toast.makeText(
                        this,
                        "Failed to submit input: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateOutput() {
        try {
            if (outputFile?.exists() == true) {
                val output = outputFile?.readText() ?: ""
                if (output.isNotBlank()) {
                    handler.post {
                        outputTextView.text = output
                    }
                }
            } else {
                handler.post {
                    if (!outputTextView.text.contains("Waiting for output file")) {
                        outputTextView.append("Waiting for output file...\n")
                    }
                }
            }
        } catch (e: Exception) {
            handler.post {
                if (!outputTextView.text.contains("Error reading output")) {
                    outputTextView.append("Error reading output: ${e.message}\n")
                }
            }
        }
    }

    // Check if challenge is completed successfully
    private fun checkChallengeCompletion() {
        try {
            val output = outputFile?.readText()?.trim() ?: ""
            val expected = correctOutput.trim()

            if (output.contains(expected)) {
                Toast.makeText(this, "Challenge completed successfully! 🎉", Toast.LENGTH_LONG).show()
                // You could add code here to update user progress, add points, etc.
            } else {
                Toast.makeText(this, "Output doesn't match expected result. Try again!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error checking completion: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
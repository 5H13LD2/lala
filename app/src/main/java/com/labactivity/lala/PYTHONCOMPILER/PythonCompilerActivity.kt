package com.labactivity.lala.PYTHONCOMPILER

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.labactivity.lala.UTILS.DialogUtils
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.labactivity.lala.R
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PythonCompilerActivity : AppCompatActivity() {

    private lateinit var codeEditText: EditText
    private lateinit var runButton: Button
    private lateinit var outputTextView: TextView
    private lateinit var inputContainer: LinearLayout
    private lateinit var userInputEditText: EditText
    private lateinit var submitInputButton: Button
    private lateinit var backButton: ImageButton

    private val handler = Handler(Looper.getMainLooper())
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private var pythonExecutionActive = false
    private var checkInputTimer: Runnable? = null
    private var outputFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compiler)

        // Initialize Python if not already started
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        // Find UI elements
        codeEditText = findViewById(R.id.codeEditText)
        runButton = findViewById(R.id.runButton)
        outputTextView = findViewById(R.id.outputTextView)
        inputContainer = findViewById(R.id.inputContainer)
        userInputEditText = findViewById(R.id.userInputEditText)
        submitInputButton = findViewById(R.id.submitInputButton)
        backButton = findViewById(R.id.backButton)

        // Enable scrolling for output
        outputTextView.movementMethod = ScrollingMovementMethod()

        // Optional: Starter code with input example
        codeEditText.setText(
            """
           for i in range(5):
               print(f"Line {i}: Hello, TechLauncher!")
            """.trimIndent()
        )

        // Set click listener for Run button
        runButton.setOnClickListener {
            executePythonScript()
        }

        // Set click listener for Submit Input button
        submitInputButton.setOnClickListener {
            val userInput = userInputEditText.text.toString()
            submitUserInput(userInput)
            userInputEditText.text.clear()
        }

        // Set click listener for Back button
        backButton.setOnClickListener {
            finish()
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

    private fun executePythonScript() {
        val userCode = codeEditText.text.toString().trim()

        if (userCode.isBlank()) {
            DialogUtils.showWarningDialog(this, "Empty Code", "Please enter some Python code.")
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
                    DialogUtils.showErrorDialog(this, "Execution Error", "Execution failed: ${e.message}")
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

                        handler.post {
                            inputContainer.visibility = View.VISIBLE
                            userInputEditText.hint = prompt.ifEmpty { "Enter input..." }
                            // Request focus and show keyboard
                            userInputEditText.requestFocus()
                        }
                    } else {
                        handler.post {
                            if (inputContainer.visibility == View.VISIBLE) {
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
                    DialogUtils.showErrorDialog(
                        this,
                        "Input Error",
                        "Failed to submit input: ${e.message}"
                    )
                }
            }
        }
    }

    private fun updateOutput() {
        try {
            if (outputFile?.exists() == true) {
                var output = outputFile?.readText() ?: ""
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
}
package com.labactivity.lala

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File

class PythonCompilerActivity : AppCompatActivity() {

    private lateinit var codeEditText: EditText
    private lateinit var runButton: Button
    private lateinit var outputTextView: TextView

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

        // Optional: Starter code
        codeEditText.setText(
            """
            print("Hello from Python!")
            """.trimIndent()
        )

        // Set click listener for Run button
        runButton.setOnClickListener {
            executePythonScript()
        }
    }

    private fun executePythonScript() {
        val userCode = codeEditText.text.toString().trim()

        if (userCode.isBlank()) {
            Toast.makeText(this, "Please enter some Python code.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Start Python environment if not already started
            val py = Python.getInstance()

            // Get the Python module that contains our execution function
            val pyModule = py.getModule("myscript")

            // Call the function that executes user code and pass the code as parameter
            pyModule.callAttr("execute_code", userCode)

            // Try several possible file locations
            val fileDir = applicationContext.filesDir
            val possibleLocations = listOf(
                File(fileDir, "files/code_output.txt"),
                File(fileDir.parentFile, "files/code_output.txt"),
                File(fileDir, "code_output.txt"),
                File(fileDir.parentFile, "code_output.txt")
            )

            var outputFound = false

            // Try to read from each possible location
            for (outputFile in possibleLocations) {
                if (outputFile.exists()) {
                    val output = outputFile.readText()
                    outputTextView.text = output
                    outputFound = true
                    Toast.makeText(
                        this,
                        "Found output at: ${outputFile.absolutePath}",
                        Toast.LENGTH_SHORT
                    ).show()
                    break
                }
            }

            // If no output file was found, display diagnostic information
            if (!outputFound) {
                val pyResult = pyModule.callAttr("get_file_info").toString()
                outputTextView.text = "No output file found.\n\nPython diagnostic info:\n$pyResult"
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Execution failed: ${e.message}", Toast.LENGTH_SHORT).show()
            outputTextView.text = "Error: ${e.message}\n\nStack trace: ${e.stackTraceToString()}"
            e.printStackTrace()
        }
    }
}
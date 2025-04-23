package com.labactivity.lala

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class PythonCompilerActivity : AppCompatActivity() {

    private lateinit var codeEditText: EditText
    private lateinit var runButton: Button
    private lateinit var outputTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compiler)

        // Start Chaquopy Python instance
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        // UI Bindings
        codeEditText = findViewById(R.id.codeEditText)
        runButton = findViewById(R.id.runButton)
        outputTextView = findViewById(R.id.outputTextView)

        // Sample starter code
        codeEditText.setText(
            """
            # Write your Python code here
            print("Hello World")
            
            # Try some math
            result = 5 + 7
            print(f"5 + 7 = {result}")
            """.trimIndent()
        )

        runButton.setOnClickListener {
            executeCode()
        }
    }

    private fun executeCode() {
        val userCode = codeEditText.text.toString().trimIndent()
        val executor = PythonExecutor()
        val result = executor.execute(userCode)
        outputTextView.text = result
    }
}

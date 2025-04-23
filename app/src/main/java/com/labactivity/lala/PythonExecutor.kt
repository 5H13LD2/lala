package com.labactivity.lala

import com.chaquo.python.PyObject
import com.chaquo.python.Python

class PythonExecutor {

    private val py: Python = Python.getInstance()
    private val pyMain: PyObject = py.getModule("__main__")
    private val builtins: PyObject = py.getBuiltins()

    private val globals: PyObject =
        pyMain["__dict__"] ?: throw Exception("Globals dictionary is null")

    init {
        // Setup code to capture stdout
        val setupCode = """
            import sys
            from io import StringIO
            capture_buffer = StringIO()
            sys.stdout = capture_buffer
        """.trimIndent()

        // Execute setup code using globals context
        builtins.callAttr("exec", setupCode, globals)
    }

    fun execute(userCode: String): String {
        return try {
            // Properly format the user code to avoid indentation errors
            val formattedCode = formatUserCode(userCode)

            // Simplify execution without wrapping try-except block
            val wrappedCode = """
                exec('''$formattedCode''')
            """.trimIndent()

            // Execute the user code with globals context
            builtins.callAttr("exec", wrappedCode, globals)

            // Retrieve the captured output
            val output = pyMain.callAttr("capture_buffer.getvalue")
            output.toString()
        } catch (e: Exception) {
            "Execution failed: ${e.message}"
        }
    }

    // Function to clean up the user's code by stripping indentation issues
    private fun formatUserCode(userCode: String): String {
        return userCode.lines()
            .map { it.trim() } // Trim each line to avoid extra indentation
            .joinToString("\n") // Rebuild the code into a single string
    }

    // Function to check if the user code has syntax errors or indentation issues
    private fun checkSyntax(code: String) {
        try {
            py.getModule("__main__").callAttr("compile", code, "<string>", "exec")
        } catch (e: Exception) {
            throw IllegalArgumentException("Syntax or indentation error: ${e.message}")
        }
    }
}

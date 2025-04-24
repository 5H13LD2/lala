package com.labactivity.lala

import com.chaquo.python.PyObject
import com.chaquo.python.Python

class PythonExecutor {

    private val py: Python = Python.getInstance()
    private val pyMain: PyObject = py.getModule("__main__")
    private val builtins: PyObject = py.getBuiltins()

    private val globals: PyObject = builtins.callAttr("dict") // Correctly create a dictionary for globals

    init {
        // Setup code to capture stdout
        val setupCode = """
            import sys
            from io import StringIO
            capture_buffer = StringIO()
            sys.stdout = capture_buffer
        """.trimIndent()

        // Execute setup code using the globals context explicitly
        println("Executing setup code to redirect stdout")
        try {
            builtins.callAttr("exec", setupCode, globals, globals)
        } catch (e: Exception) {
            println("Failed to initialize stdout capture: ${e.message}")
            throw RuntimeException("Initialization failed: ${e.message}")
        }
    }

    fun execute(userCode: String): String {
        return try {
            println("Original user code:")
            println(userCode)
            // First, check the syntax
            checkSyntax(userCode)

            // Properly format the user code to avoid indentation errors
            val formattedCode = formatUserCode(userCode)
            println("Formatted user code:")
            println(formattedCode)

            // Wrap the code within a Python function to avoid indentation issues
            val wrappedCode = """
                def execute_user_code():
${formattedCode.prependIndent("    ")}
                execute_user_code()
            """.trimIndent()

            println("Wrapped user code:")
            println(wrappedCode)

            // Check syntax of wrapped code to catch indentation issues
            checkSyntax(wrappedCode)

            // Execute the wrapped code with globals context
            builtins.callAttr("exec", wrappedCode, globals, globals)

            // Retrieve the captured output directly from the globals dictionary
            val output = globals["capture_buffer"]?.callAttr("getvalue")?.toString() ?: ""
            println("Captured output:")
            println(output)

            output
        } catch (e: Exception) {
            e.printStackTrace() // Print the full stack trace for debugging
            println("Execution failed: ${e.message}")
            "Execution failed: ${e.message}"
        }
    }

    // Function to clean up the user's code by normalizing indentation
    private fun formatUserCode(userCode: String): String {
        if (userCode.isBlank()) {
            println("User code is empty or blank")
            return ""
        }

        // Replace tabs with spaces for consistency (Python expects 4 spaces per indent)
        val normalizedCode = userCode.replace("\t", "    ")

        // Split into lines and process each line
        val lines = normalizedCode.lines()
            .map { it.replace("\r", "") } // Remove carriage returns
            .map { it.trimEnd() } // Remove trailing whitespace
            .filter { it.isNotBlank() || it.isEmpty() } // Keep empty lines but remove lines with only whitespace

        println("Normalized lines after replacing tabs and cleaning:")
        lines.forEach { println("'$it'") }

        if (lines.isEmpty()) {
            println("No valid lines after normalization")
            return ""
        }

        // Find the minimum indentation level (ignoring empty lines)
        val nonEmptyLines = lines.filter { it.isNotBlank() }
        val minIndentation = if (nonEmptyLines.isNotEmpty()) {
            nonEmptyLines
                .map { it.takeWhile { ch -> ch == ' ' }.length }
                .minOrNull() ?: 0
        } else {
            0
        }
        println("Minimum indentation detected: $minIndentation")

        // Remove the common indentation and ensure consistent 4-space indentation
        val formattedLines = lines.map { line ->
            if (line.isBlank()) {
                "" // Preserve empty lines as empty
            } else {
                val trimmed = line.drop(minIndentation)
                // Ensure indentation is a multiple of 4 spaces
                val currentIndent = line.takeWhile { ch -> ch == ' ' }.length - minIndentation
                val normalizedIndent = (currentIndent / 4) * 4
                "    ".repeat(normalizedIndent / 4) + trimmed.trimStart()
            }
        }

        println("Formatted lines after indentation normalization:")
        formattedLines.forEach { println("'$it'") }

        return formattedLines.joinToString("\n")
    }

    // Function to check if the code has syntax or indentation issues
    private fun checkSyntax(code: String) {
        if (code.isBlank()) {
            println("Code is empty, skipping syntax check")
            return
        }

        try {
            println("Compiling code to check for syntax errors...")
            // Use the "compile" function from builtins to check for syntax errors
            builtins.callAttr("compile", code, "<string>", "exec")
        } catch (e: Exception) {
            println("Syntax error detected: ${e.message}")
            throw IllegalArgumentException("Syntax or indentation error: ${e.message}")
        }
    }

    // Function to escape special characters in user input (optional, if needed)
    private fun escapeUserCode(userCode: String): String {
        return userCode.replace("'''", "\\'\\'\\'").replace("\"\"\"", "\\\"\\\"\"")
    }
}
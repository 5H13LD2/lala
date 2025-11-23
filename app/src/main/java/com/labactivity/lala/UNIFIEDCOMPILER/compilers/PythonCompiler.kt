package com.labactivity.lala.UNIFIEDCOMPILER.compilers

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.labactivity.lala.UNIFIEDCOMPILER.CourseCompiler
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerConfig
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Python compiler implementation using Chaquopy
 */
class PythonCompiler(private val context: Context) : CourseCompiler {

    init {
        // Initialize Python if not already started
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
    }

    override suspend fun compile(code: String, config: CompilerConfig): CompilerResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            withTimeout(config.timeout) {
                val python = Python.getInstance()
                val output = ByteArrayOutputStream()
                val errorOutput = ByteArrayOutputStream()

                // Redirect stdout and stderr
                val originalOut = System.out
                val originalErr = System.err
                System.setOut(PrintStream(output))
                System.setErr(PrintStream(errorOutput))

                try {
                    // Execute code directly using Python's built-in exec function
                    val builtins = python.getBuiltins()

                    // If stdin input is provided, make it available
                    if (config.enableStdin && config.stdinInput.isNotEmpty()) {
                        // Execute with stdin support
                        val wrappedCode = """
import sys
from io import StringIO
sys.stdin = StringIO('''${config.stdinInput}''')

$code
                        """.trimIndent()
                        builtins.callAttr("exec", wrappedCode)
                    } else {
                        // Execute code directly
                        builtins.callAttr("exec", code)
                    }

                    val executionTime = System.currentTimeMillis() - startTime
                    val outputStr = output.toString().trim()

                    // Test case validation if provided
                    var testCasesPassed = 0
                    if (config.testCases.isNotEmpty()) {
                        testCasesPassed = validateTestCases(code, config.testCases)
                    }

                    CompilerResult(
                        success = true,
                        output = outputStr.take(config.maxOutputLength),
                        executionTime = executionTime,
                        compiledSuccessfully = true,
                        testCasesPassed = testCasesPassed,
                        totalTestCases = config.testCases.size
                    )
                } catch (e: Exception) {
                    val executionTime = System.currentTimeMillis() - startTime
                    val errorStr = errorOutput.toString().ifEmpty { e.message ?: "Unknown error" }

                    CompilerResult(
                        success = false,
                        output = output.toString(),
                        error = errorStr,
                        executionTime = executionTime,
                        compiledSuccessfully = false
                    )
                } finally {
                    // Restore original streams
                    System.setOut(originalOut)
                    System.setErr(originalErr)
                }
            }
        } catch (e: Exception) {
            CompilerResult(
                success = false,
                output = "",
                error = "Execution timeout or error: ${e.message}",
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )
        }
    }

    private fun validateTestCases(code: String, testCases: List<com.labactivity.lala.UNIFIEDCOMPILER.models.TestCase>): Int {
        var passed = 0
        val python = Python.getInstance()
        val builtins = python.getBuiltins()

        testCases.forEach { testCase ->
            try {
                val output = ByteArrayOutputStream()
                val originalOut = System.out
                System.setOut(PrintStream(output))

                try {
                    if (testCase.input.isNotEmpty()) {
                        val wrappedCode = """
import sys
from io import StringIO
sys.stdin = StringIO('''${testCase.input}''')

$code
                        """.trimIndent()
                        builtins.callAttr("exec", wrappedCode)
                    } else {
                        builtins.callAttr("exec", code)
                    }

                    val actualOutput = output.toString().trim()
                    if (actualOutput == testCase.expectedOutput.trim()) {
                        passed++
                    }
                } finally {
                    System.setOut(originalOut)
                }
            } catch (e: Exception) {
                // Test case failed
            }
        }
        return passed
    }

    override fun getLanguageId(): String = "python"

    override fun getLanguageName(): String = "Python"

    override fun getFileExtension(): String = ".py"

    override fun validateSyntax(code: String): String? {
        return try {
            val python = Python.getInstance()
            python.getBuiltins().callAttr("compile", code, "<string>", "exec")
            null // No syntax errors
        } catch (e: Exception) {
            e.message // Return error message
        }
    }
}

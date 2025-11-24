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

/**
 * Python compiler implementation using Chaquopy
 * Uses Python's native sys module for proper stdout/stderr capture
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

                try {
                    // Use Python to capture output within Python itself
                    val captureScript = """
import sys
from io import StringIO

# Create StringIO objects to capture stdout and stderr
_stdout_capture = StringIO()
_stderr_capture = StringIO()

# Save originals
_original_stdout = sys.stdout
_original_stderr = sys.stderr

# Redirect to capture
sys.stdout = _stdout_capture
sys.stderr = _stderr_capture

_exec_error = None

try:
    # Execute user code
    exec('''$code''')
except Exception as e:
    import traceback
    _exec_error = str(e)
    traceback.print_exc()
finally:
    # Restore originals
    sys.stdout = _original_stdout
    sys.stderr = _original_stderr

# Get captured output
_output = _stdout_capture.getvalue()
_errors = _stderr_capture.getvalue()
                    """.trimIndent()

                    // Execute the capture script
                    val builtins = python.getBuiltins()
                    val mainModule = python.getModule("__main__")
                    val globalsDict = mainModule.get("__dict__")

                    builtins.callAttr("exec", captureScript, globalsDict)

                    // Get the results from globals
                    val output = globalsDict?.callAttr("get", "_output")?.toString() ?: ""
                    val errors = globalsDict?.callAttr("get", "_errors")?.toString() ?: ""
                    val execError = globalsDict?.callAttr("get", "_exec_error")

                    val executionTime = System.currentTimeMillis() - startTime

                    val finalOutput = output.ifEmpty {
                        if (execError == null || execError.toString() == "None") {
                            "Code executed successfully (no output)"
                        } else {
                            ""
                        }
                    }

                    val hasError = errors.isNotEmpty() || (execError != null && execError.toString() != "None")

                    CompilerResult(
                        success = !hasError,
                        output = finalOutput.take(config.maxOutputLength),
                        error = if (hasError) errors.ifEmpty { execError?.toString() } else null,
                        executionTime = executionTime,
                        compiledSuccessfully = true,
                        testCasesPassed = 0,
                        totalTestCases = 0
                    )

                } catch (e: Exception) {
                    val executionTime = System.currentTimeMillis() - startTime
                    val errorMsg = e.message ?: e.toString()

                    CompilerResult(
                        success = false,
                        output = "",
                        error = "Python Error: $errorMsg",
                        executionTime = executionTime,
                        compiledSuccessfully = false
                    )
                }
            }
        } catch (e: Exception) {
            CompilerResult(
                success = false,
                output = "",
                error = "Execution timeout (${config.timeout}ms exceeded)",
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )
        }
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

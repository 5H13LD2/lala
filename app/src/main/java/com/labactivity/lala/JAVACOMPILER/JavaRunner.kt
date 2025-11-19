package com.labactivity.lala.JAVACOMPILER

import android.util.Log
import org.codehaus.commons.compiler.CompileException
import org.codehaus.janino.SimpleCompiler
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.reflect.Method

/**
 * JavaRunner - Compiles and executes Java code dynamically using Janino
 * Similar to PythonRunner for Python execution
 */
class JavaRunner {

    companion object {
        private const val TAG = "JavaRunner"
    }

    /**
     * Result class to hold execution output and errors
     */
    data class ExecutionResult(
        val output: String,
        val error: String? = null,
        val success: Boolean = true
    )

    /**
     * Compile and execute Java code
     * @param javaCode The complete Java class code as a string
     * @param className The name of the class to instantiate
     * @param methodName The name of the method to invoke (default: "run")
     * @return ExecutionResult containing output or error
     */
    fun executeJavaCode(
        javaCode: String,
        className: String = "Test",
        methodName: String = "run"
    ): ExecutionResult {
        val outputStream = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()
        val originalOut = System.out
        val originalErr = System.err

        try {
            // Redirect System.out and System.err to capture output
            System.setOut(PrintStream(outputStream))
            System.setErr(PrintStream(errorStream))

            Log.d(TAG, "Compiling Java code...")

            // Create Janino SimpleCompiler
            val compiler = SimpleCompiler()

            // Compile the Java source code
            compiler.cook(javaCode)

            Log.d(TAG, "Compilation successful, loading class: $className")

            // Load the compiled class
            val classLoader = compiler.classLoader
            val compiledClass = classLoader.loadClass(className)

            Log.d(TAG, "Class loaded, creating instance...")

            // Create an instance of the class
            val instance = compiledClass.getDeclaredConstructor().newInstance()

            Log.d(TAG, "Instance created, invoking method: $methodName")

            // Find and invoke the method
            val method: Method = compiledClass.getMethod(methodName)
            val result = method.invoke(instance)

            // Restore original System.out and System.err
            System.setOut(originalOut)
            System.setErr(originalErr)

            // Get captured output
            val output = outputStream.toString()
            val errors = errorStream.toString()

            // Combine method result with printed output
            val finalOutput = buildString {
                if (output.isNotEmpty()) {
                    append(output)
                }
                if (result != null) {
                    if (output.isNotEmpty()) append("\n")
                    append("Return value: $result")
                }
                if (isEmpty()) {
                    append("Execution completed successfully (no output)")
                }
            }

            return if (errors.isEmpty()) {
                ExecutionResult(
                    output = finalOutput,
                    success = true
                )
            } else {
                ExecutionResult(
                    output = finalOutput,
                    error = errors,
                    success = false
                )
            }

        } catch (e: CompileException) {
            // Restore original streams
            System.setOut(originalOut)
            System.setErr(originalErr)

            Log.e(TAG, "Compilation error", e)
            return ExecutionResult(
                output = "",
                error = "Compilation Error:\n${e.message}",
                success = false
            )

        } catch (e: ClassNotFoundException) {
            // Restore original streams
            System.setOut(originalOut)
            System.setErr(originalErr)

            Log.e(TAG, "Class not found: $className", e)
            return ExecutionResult(
                output = "",
                error = "Class '$className' not found. Make sure the class name matches.",
                success = false
            )

        } catch (e: NoSuchMethodException) {
            // Restore original streams
            System.setOut(originalOut)
            System.setErr(originalErr)

            Log.e(TAG, "Method not found: $methodName", e)
            return ExecutionResult(
                output = "",
                error = "Method '$methodName' not found in class '$className'.",
                success = false
            )

        } catch (e: Exception) {
            // Restore original streams
            System.setOut(originalOut)
            System.setErr(originalErr)

            Log.e(TAG, "Execution error", e)
            return ExecutionResult(
                output = outputStream.toString(),
                error = "Runtime Error:\n${e.message ?: e.javaClass.simpleName}",
                success = false
            )
        }
    }

    /**
     * Validates if the Java code compiles without executing it
     * @param javaCode The Java code to validate
     * @return Pair<Boolean, String?> - success flag and error message if any
     */
    fun validateJavaCode(javaCode: String): Pair<Boolean, String?> {
        return try {
            val compiler = SimpleCompiler()
            compiler.cook(javaCode)
            Pair(true, null)
        } catch (e: CompileException) {
            Pair(false, "Compilation Error:\n${e.message}")
        } catch (e: Exception) {
            Pair(false, "Error:\n${e.message}")
        }
    }

    /**
     * Test helper to quickly execute a simple Java snippet
     * Wraps the code in a Test class with a run() method
     */
    fun executeSimpleCode(code: String): ExecutionResult {
        val wrappedCode = """
            public class Test {
                public void run() {
                    $code
                }
            }
        """.trimIndent()

        return executeJavaCode(wrappedCode, "Test", "run")
    }
}

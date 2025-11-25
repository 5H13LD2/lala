package com.labactivity.lala.UNIFIEDCOMPILER.compilers

import android.util.Log
import com.labactivity.lala.UNIFIEDCOMPILER.CourseCompiler
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerConfig
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.codehaus.commons.compiler.CompileException
import org.codehaus.janino.SimpleCompiler
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Java compiler implementation using Janino
 * Supports both main() methods and regular methods
 *
 * IMPORTANT: Requires Janino dependency in build.gradle:
 * implementation 'org.codehaus.janino:janino:3.1.9'
 */
class JavaCompiler : CourseCompiler {

    companion object {
        private const val TAG = "JavaCompiler"
    }

    override suspend fun compile(code: String, config: CompilerConfig): CompilerResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val outputStream = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()
        val originalOut = System.out
        val originalErr = System.err

        // Validate input
        if (code.isBlank()) {
            return@withContext CompilerResult(
                success = false,
                output = "",
                error = "No code to execute",
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )
        }

        try {
            withTimeout(config.timeout) {
                try {
                    // Redirect System.out and System.err BEFORE any compilation/execution
                    System.setOut(PrintStream(outputStream, true))
                    System.setErr(PrintStream(errorStream, true))

                    // Extract class name from code first
                    val className = extractClassName(code)
                    if (className == null) {
                        System.setOut(originalOut)
                        System.setErr(originalErr)

                        Log.e(TAG, "No class definition found in code")
                        return@withTimeout CompilerResult(
                            success = false,
                            output = "",
                            error = "Error: No class definition found.\n\nPlease define a class like:\npublic class Main {\n    public static void main(String[] args) {\n        // your code here\n    }\n}",
                            executionTime = System.currentTimeMillis() - startTime,
                            compiledSuccessfully = false
                        )
                    }

                    Log.d(TAG, "Compiling class: $className")

                    // Create Janino SimpleCompiler
                    val compiler = SimpleCompiler()

                    // Set parent class loader - use Thread's context class loader for Android compatibility
                    compiler.setParentClassLoader(Thread.currentThread().contextClassLoader
                        ?: this@JavaCompiler::class.java.classLoader)

                    // Attempt compilation
                    try {
                        compiler.cook(code)
                        Log.d(TAG, "Compilation successful")
                    } catch (e: CompileException) {
                        System.setOut(originalOut)
                        System.setErr(originalErr)

                        val errorMessage = formatCompileError(e)
                        Log.e(TAG, "Compilation failed: $errorMessage")

                        return@withTimeout CompilerResult(
                            success = false,
                            output = "",
                            error = errorMessage,
                            executionTime = System.currentTimeMillis() - startTime,
                            compiledSuccessfully = false
                        )
                    }

                    // Load the compiled class
                    val classLoader = compiler.classLoader
                    val compiledClass = try {
                        classLoader.loadClass(className)
                    } catch (e: ClassNotFoundException) {
                        System.setOut(originalOut)
                        System.setErr(originalErr)

                        Log.e(TAG, "Class not found after compilation: $className")
                        return@withTimeout CompilerResult(
                            success = false,
                            output = "",
                            error = "Error: Class '$className' not found after compilation.\n\nMake sure your class name matches the one in your code.",
                            executionTime = System.currentTimeMillis() - startTime,
                            compiledSuccessfully = false
                        )
                    } catch (e: Exception) {
                        System.setOut(originalOut)
                        System.setErr(originalErr)

                        Log.e(TAG, "Error loading class: $className", e)
                        return@withTimeout CompilerResult(
                            success = false,
                            output = "",
                            error = "Error loading class '$className': ${e.javaClass.simpleName}: ${e.message}",
                            executionTime = System.currentTimeMillis() - startTime,
                            compiledSuccessfully = false
                        )
                    }

                    // Determine which method to execute
                    val methodToRun = findExecutableMethod(compiledClass, code)

                    Log.d(TAG, "Executing method: ${methodToRun.name} (static: ${methodToRun.isStatic})")

                    // Execute the method
                    val result = try {
                        executeMethod(compiledClass, methodToRun)
                    } catch (e: java.lang.reflect.InvocationTargetException) {
                        // Exception thrown by the user's code
                        val cause = e.targetException ?: e.cause ?: e
                        System.setOut(originalOut)
                        System.setErr(originalErr)

                        val errorDetails = formatRuntimeError(cause, className)
                        Log.e(TAG, "Runtime error in user code", cause)

                        return@withTimeout CompilerResult(
                            success = false,
                            output = outputStream.toString().trimEnd(),
                            error = errorDetails,
                            executionTime = System.currentTimeMillis() - startTime,
                            compiledSuccessfully = true // Code compiled, but had runtime error
                        )
                    } catch (e: NoSuchMethodException) {
                        System.setOut(originalOut)
                        System.setErr(originalErr)

                        Log.e(TAG, "Method not found: ${methodToRun.name}")
                        return@withTimeout CompilerResult(
                            success = false,
                            output = outputStream.toString().trimEnd(),
                            error = "Error: No executable method found in class '$className'.\n\nAdd a main method:\npublic static void main(String[] args) { }",
                            executionTime = System.currentTimeMillis() - startTime,
                            compiledSuccessfully = true
                        )
                    } catch (e: IllegalAccessException) {
                        System.setOut(originalOut)
                        System.setErr(originalErr)

                        Log.e(TAG, "Cannot access method: ${methodToRun.name}", e)
                        return@withTimeout CompilerResult(
                            success = false,
                            output = outputStream.toString().trimEnd(),
                            error = "Error: Cannot access method '${methodToRun.name}'. Make sure it's public.",
                            executionTime = System.currentTimeMillis() - startTime,
                            compiledSuccessfully = true
                        )
                    } catch (e: InstantiationException) {
                        System.setOut(originalOut)
                        System.setErr(originalErr)

                        Log.e(TAG, "Cannot instantiate class: $className", e)
                        return@withTimeout CompilerResult(
                            success = false,
                            output = outputStream.toString().trimEnd(),
                            error = "Error: Cannot create instance of '$className'. Make sure it has a default constructor.",
                            executionTime = System.currentTimeMillis() - startTime,
                            compiledSuccessfully = true
                        )
                    }

                    // Restore streams
                    System.setOut(originalOut)
                    System.setErr(originalErr)

                    val output = outputStream.toString().trimEnd()
                    val errors = errorStream.toString().trimEnd()
                    val executionTime = System.currentTimeMillis() - startTime

                    // Build final output
                    val finalOutput = when {
                        output.isNotEmpty() -> output
                        result != null && result.toString() != "null" && result != Unit -> "Return value: $result"
                        else -> "Code executed successfully (no output)"
                    }

                    Log.d(TAG, "Execution completed successfully in ${executionTime}ms")

                    CompilerResult(
                        success = errors.isEmpty(),
                        output = finalOutput.take(config.maxOutputLength),
                        error = errors.ifEmpty { null },
                        executionTime = executionTime,
                        compiledSuccessfully = true,
                        testCasesPassed = 0,
                        totalTestCases = 0
                    )

                } catch (e: Exception) {
                    System.setOut(originalOut)
                    System.setErr(originalErr)

                    Log.e(TAG, "Unexpected error during execution", e)

                    CompilerResult(
                        success = false,
                        output = outputStream.toString().trimEnd(),
                        error = "Unexpected Error: ${e.javaClass.simpleName}: ${e.message}",
                        executionTime = System.currentTimeMillis() - startTime,
                        compiledSuccessfully = false
                    )
                }
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            System.setOut(originalOut)
            System.setErr(originalErr)

            Log.w(TAG, "Execution timeout")

            CompilerResult(
                success = false,
                output = outputStream.toString().trimEnd(),
                error = "Execution timeout (${config.timeout}ms exceeded).\n\nYour code may have an infinite loop or is taking too long to execute.",
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )
        } catch (e: Exception) {
            System.setOut(originalOut)
            System.setErr(originalErr)

            Log.e(TAG, "System error during execution", e)

            CompilerResult(
                success = false,
                output = "",
                error = "System Error: ${e.message ?: "Unknown error occurred"}",
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )
        }
    }

    override fun getLanguageId(): String = "java"

    override fun getLanguageName(): String = "Java"

    override fun getFileExtension(): String = ".java"

    override fun validateSyntax(code: String): String? {
        return try {
            val compiler = SimpleCompiler()
            compiler.setParentClassLoader(Thread.currentThread().contextClassLoader
                ?: this::class.java.classLoader)
            compiler.cook(code)
            null
        } catch (e: CompileException) {
            formatCompileError(e)
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    override fun supportsTestCases(): Boolean = true

    /**
     * Data class to hold method execution info
     */
    private data class MethodInfo(
        val name: String,
        val isStatic: Boolean,
        val parameterTypes: Array<Class<*>> = emptyArray()
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is MethodInfo) return false
            return name == other.name && isStatic == other.isStatic && parameterTypes.contentEquals(other.parameterTypes)
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + isStatic.hashCode()
            result = 31 * result + parameterTypes.contentHashCode()
            return result
        }
    }

    /**
     * Extract class name from Java code
     */
    private fun extractClassName(code: String): String? {
        // Match class declaration (public, default, or with other modifiers)
        val classPattern = Regex("""(?:public\s+|private\s+|protected\s+)?(?:final\s+|abstract\s+)?class\s+(\w+)""")
        return classPattern.find(code)?.groupValues?.get(1)
    }

    /**
     * Find the best method to execute in the compiled class
     */
    private fun findExecutableMethod(compiledClass: Class<*>, code: String): MethodInfo {
        // Priority order:
        // 1. public static void main(String[] args)
        // 2. public void run()
        // 3. public void execute()
        // 4. First public static method
        // 5. First public method

        val methods = compiledClass.declaredMethods

        // Check for main method
        if (code.contains("public static void main")) {
            return MethodInfo("main", true, arrayOf(Array<String>::class.java))
        }

        // Check for run method
        methods.find { it.name == "run" && it.parameterCount == 0 }?.let {
            return MethodInfo("run", java.lang.reflect.Modifier.isStatic(it.modifiers))
        }

        // Check for execute method
        methods.find { it.name == "execute" && it.parameterCount == 0 }?.let {
            return MethodInfo("execute", java.lang.reflect.Modifier.isStatic(it.modifiers))
        }

        // Find first public static method with no parameters
        methods.find {
            java.lang.reflect.Modifier.isStatic(it.modifiers) &&
                    java.lang.reflect.Modifier.isPublic(it.modifiers) &&
                    it.parameterCount == 0 &&
                    it.name != "main"
        }?.let {
            return MethodInfo(it.name, true)
        }

        // Find first public method with no parameters
        methods.find {
            java.lang.reflect.Modifier.isPublic(it.modifiers) &&
                    it.parameterCount == 0
        }?.let {
            return MethodInfo(it.name, java.lang.reflect.Modifier.isStatic(it.modifiers))
        }

        // Default to main
        return MethodInfo("main", true, arrayOf(Array<String>::class.java))
    }

    /**
     * Execute a method on the compiled class
     */
    private fun executeMethod(compiledClass: Class<*>, methodInfo: MethodInfo): Any? {
        return if (methodInfo.isStatic) {
            // Static method - no instance needed
            val method = if (methodInfo.parameterTypes.isNotEmpty()) {
                compiledClass.getDeclaredMethod(methodInfo.name, *methodInfo.parameterTypes)
            } else {
                compiledClass.getDeclaredMethod(methodInfo.name)
            }
            method.isAccessible = true

            if (methodInfo.name == "main" && methodInfo.parameterTypes.isNotEmpty()) {
                method.invoke(null, arrayOf<String>() as Any)
            } else {
                method.invoke(null)
            }
        } else {
            // Instance method - create instance first
            val constructor = compiledClass.getDeclaredConstructor()
            constructor.isAccessible = true
            val instance = constructor.newInstance()

            val method = compiledClass.getDeclaredMethod(methodInfo.name)
            method.isAccessible = true
            method.invoke(instance)
        }
    }

    /**
     * Format compilation errors for better readability
     */
    private fun formatCompileError(e: CompileException): String {
        val message = e.message ?: return "Compilation Error: Unknown error"

        // Try to extract line and column info
        val locationPattern = Regex("""Line (\d+), Column (\d+)""")
        val match = locationPattern.find(message)

        return if (match != null) {
            val line = match.groupValues[1]
            val column = match.groupValues[2]
            "Compilation Error (Line $line, Col $column):\n${message.substringAfter(": ").trim()}"
        } else {
            "Compilation Error:\n$message"
        }
    }

    /**
     * Format runtime errors for better readability
     */
    private fun formatRuntimeError(cause: Throwable, className: String): String {
        val errorType = cause.javaClass.simpleName
        val errorMessage = cause.message ?: "No details available"

        // Extract relevant stack trace (only user code, not internal frames)
        val relevantStackTrace = cause.stackTrace
            .filter { it.className.contains(className) || it.className == className }
            .take(3)
            .joinToString("\n") { "  at ${it.className}.${it.methodName}(line ${it.lineNumber})" }

        return buildString {
            append("Runtime Error: $errorType\n")
            append("Message: $errorMessage")
            if (relevantStackTrace.isNotEmpty()) {
                append("\n\nStack trace:\n$relevantStackTrace")
            }
        }
    }
}
package com.labactivity.lala.UNIFIEDCOMPILER.compilers

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
 * Wraps the existing JavaRunner functionality
 */
class JavaCompiler : CourseCompiler {

    override suspend fun compile(code: String, config: CompilerConfig): CompilerResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            withTimeout(config.timeout) {
                val outputStream = ByteArrayOutputStream()
                val errorStream = ByteArrayOutputStream()
                val originalOut = System.out
                val originalErr = System.err

                try {
                    // Redirect System.out and System.err
                    System.setOut(PrintStream(outputStream))
                    System.setErr(PrintStream(errorStream))

                    // Create Janino SimpleCompiler
                    val compiler = SimpleCompiler()
                    compiler.cook(code)

                    // Extract class name from code
                    val className = extractClassName(code) ?: "Test"
                    val methodName = extractMethodName(code) ?: "run"

                    // Load and execute
                    val classLoader = compiler.classLoader
                    val compiledClass = classLoader.loadClass(className)
                    val instance = compiledClass.getDeclaredConstructor().newInstance()
                    val method = compiledClass.getMethod(methodName)
                    val result = method.invoke(instance)

                    // Restore streams
                    System.setOut(originalOut)
                    System.setErr(originalErr)

                    val output = outputStream.toString()
                    val errors = errorStream.toString()
                    val executionTime = System.currentTimeMillis() - startTime

                    // Build final output
                    val finalOutput = buildString {
                        if (output.isNotEmpty()) append(output)
                        if (result != null) {
                            if (output.isNotEmpty()) append("\n")
                            append("Return value: $result")
                        }
                    }.ifEmpty { "Execution completed successfully" }

                    // Test case validation
                    var testCasesPassed = 0
                    if (config.testCases.isNotEmpty()) {
                        testCasesPassed = validateTestCases(code, config.testCases)
                    }

                    CompilerResult(
                        success = errors.isEmpty(),
                        output = finalOutput.take(config.maxOutputLength),
                        error = errors.ifEmpty { null },
                        executionTime = executionTime,
                        compiledSuccessfully = true,
                        testCasesPassed = testCasesPassed,
                        totalTestCases = config.testCases.size
                    )

                } catch (e: CompileException) {
                    System.setOut(originalOut)
                    System.setErr(originalErr)

                    CompilerResult(
                        success = false,
                        output = "",
                        error = "Compilation Error: ${e.message}",
                        executionTime = System.currentTimeMillis() - startTime,
                        compiledSuccessfully = false
                    )
                } catch (e: Exception) {
                    System.setOut(originalOut)
                    System.setErr(originalErr)

                    CompilerResult(
                        success = false,
                        output = outputStream.toString(),
                        error = "Runtime Error: ${e.message ?: e.javaClass.simpleName}",
                        executionTime = System.currentTimeMillis() - startTime,
                        compiledSuccessfully = true
                    )
                }
            }
        } catch (e: Exception) {
            CompilerResult(
                success = false,
                output = "",
                error = "Timeout or execution error: ${e.message}",
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )
        }
    }

    private fun validateTestCases(code: String, testCases: List<com.labactivity.lala.UNIFIEDCOMPILER.models.TestCase>): Int {
        var passed = 0
        testCases.forEach { testCase ->
            try {
                val output = ByteArrayOutputStream()
                System.setOut(PrintStream(output))

                val compiler = SimpleCompiler()
                compiler.cook(code)

                val className = extractClassName(code) ?: "Test"
                val methodName = extractMethodName(code) ?: "run"

                val classLoader = compiler.classLoader
                val compiledClass = classLoader.loadClass(className)
                val instance = compiledClass.getDeclaredConstructor().newInstance()
                val method = compiledClass.getMethod(methodName)
                method.invoke(instance)

                val actualOutput = output.toString().trim()
                if (actualOutput == testCase.expectedOutput.trim()) {
                    passed++
                }
            } catch (e: Exception) {
                // Test case failed
            }
        }
        return passed
    }

    override fun getLanguageId(): String = "java"

    override fun getLanguageName(): String = "Java"

    override fun getFileExtension(): String = ".java"

    override fun validateSyntax(code: String): String? {
        return try {
            val compiler = SimpleCompiler()
            compiler.cook(code)
            null
        } catch (e: CompileException) {
            "Compilation Error: ${e.message}"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    /**
     * Extract class name from Java code
     */
    private fun extractClassName(code: String): String? {
        val classPattern = Regex("""(?:public\s+)?class\s+(\w+)""")
        return classPattern.find(code)?.groupValues?.get(1)
    }

    /**
     * Extract main method name from Java code (defaults to "run" if not found)
     */
    private fun extractMethodName(code: String): String? {
        val mainPattern = Regex("""public\s+static\s+void\s+main\s*\(""")
        return if (mainPattern.containsMatchIn(code)) "main" else "run"
    }
}

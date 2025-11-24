package com.labactivity.lala.UNIFIEDCOMPILER.compilers

import com.labactivity.lala.UNIFIEDCOMPILER.CourseCompiler
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerConfig
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.codehaus.commons.compiler.CompileException
import org.codehaus.janino.ScriptEvaluator
import org.codehaus.janino.SimpleCompiler
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Java compiler implementation using Janino
 * Supports both main() methods and regular methods
 */
class JavaCompiler : CourseCompiler {

    override suspend fun compile(code: String, config: CompilerConfig): CompilerResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val outputStream = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()
        val originalOut = System.out
        val originalErr = System.err

        try {
            withTimeout(config.timeout) {
                try {
                    // Redirect System.out and System.err
                    System.setOut(PrintStream(outputStream, true))
                    System.setErr(PrintStream(errorStream, true))

                    // Extract class name from code first
                    val className = extractClassName(code)
                    if (className == null) {
                        System.setOut(originalOut)
                        System.setErr(originalErr)
                        return@withTimeout CompilerResult(
                            success = false,
                            output = "",
                            error = "Error: No class definition found. Please define a class.",
                            executionTime = System.currentTimeMillis() - startTime,
                            compiledSuccessfully = false
                        )
                    }

                    val methodName = extractMethodName(code) ?: "main"

                    // Create Janino SimpleCompiler
                    val compiler = SimpleCompiler()

                    // Set parent class loader - use Thread's context class loader
                    compiler.setParentClassLoader(Thread.currentThread().contextClassLoader)

                    try {
                        compiler.cook(code)
                    } catch (e: CompileException) {
                        System.setOut(originalOut)
                        System.setErr(originalErr)
                        return@withTimeout CompilerResult(
                            success = false,
                            output = "",
                            error = "Compilation Error:\n${e.message}",
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
                        return@withTimeout CompilerResult(
                            success = false,
                            output = "",
                            error = "Error: Class '$className' not found after compilation",
                            executionTime = System.currentTimeMillis() - startTime,
                            compiledSuccessfully = false
                        )
                    } catch (e: Exception) {
                        System.setOut(originalOut)
                        System.setErr(originalErr)
                        return@withTimeout CompilerResult(
                            success = false,
                            output = "",
                            error = "Error loading class: ${e.javaClass.simpleName}: ${e.message}",
                            executionTime = System.currentTimeMillis() - startTime,
                            compiledSuccessfully = false
                        )
                    }

                    // Execute the method
                    val result = try {
                        if (methodName == "main") {
                            // For main method: public static void main(String[] args)
                            val method = compiledClass.getDeclaredMethod("main", Array<String>::class.java)
                            method.isAccessible = true
                            method.invoke(null, arrayOf<String>())
                        } else {
                            // For regular methods: create instance and invoke
                            val instance = compiledClass.getDeclaredConstructor().newInstance()
                            val method = compiledClass.getDeclaredMethod(methodName)
                            method.isAccessible = true
                            method.invoke(instance)
                        }
                    } catch (e: java.lang.reflect.InvocationTargetException) {
                        // Exception thrown by the user's code
                        val cause = e.targetException ?: e
                        System.setOut(originalOut)
                        System.setErr(originalErr)

                        val stackTrace = cause.stackTraceToString().lines().take(5).joinToString("\n")
                        return@withTimeout CompilerResult(
                            success = false,
                            output = outputStream.toString(),
                            error = "Runtime Error: ${cause.javaClass.simpleName}: ${cause.message}\n$stackTrace",
                            executionTime = System.currentTimeMillis() - startTime,
                            compiledSuccessfully = true
                        )
                    } catch (e: NoSuchMethodException) {
                        System.setOut(originalOut)
                        System.setErr(originalErr)
                        return@withTimeout CompilerResult(
                            success = false,
                            output = outputStream.toString(),
                            error = "Error: Method '$methodName' not found in class '$className'",
                            executionTime = System.currentTimeMillis() - startTime,
                            compiledSuccessfully = true
                        )
                    }

                    // Restore streams
                    System.setOut(originalOut)
                    System.setErr(originalErr)

                    val output = outputStream.toString()
                    val errors = errorStream.toString()
                    val executionTime = System.currentTimeMillis() - startTime

                    // Build final output
                    val finalOutput = if (output.isNotEmpty()) {
                        output
                    } else if (result != null && result.toString() != "null") {
                        "Return value: $result"
                    } else {
                        "Code executed successfully (no output)"
                    }

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

                    CompilerResult(
                        success = false,
                        output = outputStream.toString(),
                        error = "Unexpected Error: ${e.javaClass.simpleName}: ${e.message}",
                        executionTime = System.currentTimeMillis() - startTime,
                        compiledSuccessfully = false
                    )
                }
            }
        } catch (e: Exception) {
            System.setOut(originalOut)
            System.setErr(originalErr)

            CompilerResult(
                success = false,
                output = "",
                error = "Execution timeout (${config.timeout}ms exceeded)",
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
            compiler.setParentClassLoader(this::class.java.classLoader)
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
     * Extract method name from Java code
     */
    private fun extractMethodName(code: String): String? {
        return if (code.contains("public static void main")) {
            "main"
        } else if (code.contains("public void run")) {
            "run"
        } else {
            null
        }
    }
}

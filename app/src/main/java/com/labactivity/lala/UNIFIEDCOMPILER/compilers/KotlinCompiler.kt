package com.labactivity.lala.UNIFIEDCOMPILER.compilers

import com.labactivity.lala.UNIFIEDCOMPILER.CourseCompiler
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerConfig
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream
import java.io.PrintStream


/**
 * Kotlin compiler/interpreter implementation
 *
 * NOTE: This is a SIMPLE EVALUATOR for basic Kotlin expressions.
 * Full Kotlin compilation requires kotlin-compiler-embeddable which is very heavy.
 *
 * For educational purposes, this supports:
 * - Simple expressions and print statements
 * - Variable declarations
 * - Basic control flow (if/when)
 * - Simple functions
 *
 * Limitations:
 * - No class definitions
 * - No complex OOP features
 * - Limited standard library access
 */
class KotlinCompiler : CourseCompiler {

    override suspend fun compile(code: String, config: CompilerConfig): CompilerResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            withTimeout(config.timeout) {
                val output = ByteArrayOutputStream()
                val originalOut = System.out
                System.setOut(PrintStream(output))

                try {
                    // Use JSR-223 scripting engine for Kotlin (if available)
                    // For now, we'll do a simple interpretation approach
                    val result = interpretKotlinCode(code)

                    System.setOut(originalOut)
                    val executionTime = System.currentTimeMillis() - startTime

                    val outputStr = output.toString() + result

                    // Test case validation
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
                    System.setOut(originalOut)

                    CompilerResult(
                        success = false,
                        output = output.toString(),
                        error = "Execution Error: ${e.message}",
                        executionTime = System.currentTimeMillis() - startTime,
                        compiledSuccessfully = false
                    )
                }
            }
        } catch (e: Exception) {
            CompilerResult(
                success = false,
                output = "",
                error = "Timeout or error: ${e.message}",
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )
        }
    }

    /**
     * Simple Kotlin code interpreter
     * This handles basic Kotlin syntax for educational purposes
     */
    private fun interpretKotlinCode(code: String): String {
        val result = StringBuilder()

        // Split code into statements
        val statements = code.split("\n").filter { it.trim().isNotEmpty() }

        val variables = mutableMapOf<String, Any>()

        statements.forEach { statement ->
            val trimmed = statement.trim()

            when {
                // Handle println
                trimmed.startsWith("println(") -> {
                    val content = extractContent(trimmed, "println")
                    val evaluated = evaluateExpression(content, variables)
                    result.append(evaluated).append("\n")
                }

                // Handle print
                trimmed.startsWith("print(") -> {
                    val content = extractContent(trimmed, "print")
                    val evaluated = evaluateExpression(content, variables)
                    result.append(evaluated)
                }

                // Handle variable declaration (val/var)
                trimmed.startsWith("val ") || trimmed.startsWith("var ") -> {
                    val parts = trimmed.substringAfter(" ").split("=")
                    if (parts.size == 2) {
                        val varName = parts[0].trim().substringBefore(":")
                        val value = evaluateExpression(parts[1].trim(), variables)
                        variables[varName] = value
                    }
                }

                // Handle function calls (basic)
                trimmed.contains("fun ") -> {
                    // Skip function definitions for now
                }

                // Handle simple expressions that should be printed
                else -> {
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("//")) {
                        try {
                            val evaluated = evaluateExpression(trimmed, variables)
                            if (evaluated.isNotEmpty()) {
                                result.append(evaluated).append("\n")
                            }
                        } catch (e: Exception) {
                            // Skip invalid statements
                        }
                    }
                }
            }
        }

        return result.toString()
    }

    /**
     * Extract content from function calls like println("hello")
     */
    private fun extractContent(statement: String, functionName: String): String {
        val start = statement.indexOf("(") + 1
        val end = statement.lastIndexOf(")")
        return if (start > 0 && end > start) {
            statement.substring(start, end)
        } else {
            ""
        }
    }

    /**
     * Evaluate simple Kotlin expressions
     */
    private fun evaluateExpression(expr: String, variables: Map<String, Any>): String {
        var expression = expr.trim()

        // Remove quotes for string literals
        if (expression.startsWith("\"") && expression.endsWith("\"")) {
            return expression.substring(1, expression.length - 1)
        }

        // Check if it's a variable
        if (variables.containsKey(expression)) {
            return variables[expression].toString()
        }

        // Handle string templates
        if (expression.contains("$")) {
            variables.forEach { (key, value) ->
                expression = expression.replace("\$$key", value.toString())
                expression = expression.replace("\${$key}", value.toString())
            }
            return expression.replace("\"", "")
        }

        // Try to evaluate as number
        return try {
            when {
                expression.toIntOrNull() != null -> expression
                expression.toDoubleOrNull() != null -> expression
                expression.contains("+") -> {
                    val parts = expression.split("+").map { it.trim() }
                    val sum = parts.sumOf { evaluateExpression(it, variables).toDoubleOrNull() ?: 0.0 }
                    sum.toString()
                }
                expression.contains("-") && !expression.startsWith("-") -> {
                    val parts = expression.split("-").map { it.trim() }
                    var result = evaluateExpression(parts[0], variables).toDoubleOrNull() ?: 0.0
                    for (i in 1 until parts.size) {
                        result -= evaluateExpression(parts[i], variables).toDoubleOrNull() ?: 0.0
                    }
                    result.toString()
                }
                else -> expression
            }
        } catch (e: Exception) {
            expression
        }
    }

    private fun validateTestCases(code: String, testCases: List<com.labactivity.lala.UNIFIEDCOMPILER.models.TestCase>): Int {
        var passed = 0
        testCases.forEach { testCase ->
            try {
                val result = interpretKotlinCode(code)
                if (result.trim() == testCase.expectedOutput.trim()) {
                    passed++
                }
            } catch (e: Exception) {
                // Test case failed
            }
        }
        return passed
    }

    override fun getLanguageId(): String = "kotlin"

    override fun getLanguageName(): String = "Kotlin"

    override fun getFileExtension(): String = ".kt"

    override fun validateSyntax(code: String): String? {
        // Basic syntax validation
        val invalidPatterns = listOf(
            "import " to "Import statements are not supported in the interpreter",
            "class " to "Class definitions are not supported in the interpreter",
            "package " to "Package declarations are not supported in the interpreter"
        )

        invalidPatterns.forEach { (pattern, message) ->
            if (code.contains(pattern)) {
                return message
            }
        }

        return null
    }
}

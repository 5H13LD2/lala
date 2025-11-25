package com.labactivity.lala.UNIFIEDCOMPILER.compilers

import android.util.Log
import com.labactivity.lala.UNIFIEDCOMPILER.CourseCompiler
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerConfig
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Java compiler implementation using Piston API (Remote Execution)
 *
 * Piston is a free, open-source code execution engine.
 * No API key required!
 *
 * Supports full Java with all standard libraries.
 *
 * Note: Requires internet connection.
 */
class JavaCompiler : CourseCompiler {

    companion object {
        private const val TAG = "JavaCompiler"
        private const val PISTON_API_URL = "https://emkc.org/api/v2/piston/execute"
        private const val JAVA_VERSION = "15.0.2" // Piston's Java version
    }

    override suspend fun compile(code: String, config: CompilerConfig): CompilerResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

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

        // Extract class name for proper file naming
        val className = extractClassName(code) ?: "Main"

        // Ensure code has proper structure
        val processedCode = ensureProperStructure(code, className)

        try {
            withTimeout(config.timeout) {
                executeWithPiston(processedCode, className, startTime, config)
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.w(TAG, "Execution timeout")
            CompilerResult(
                success = false,
                output = "",
                error = "Execution timeout (${config.timeout}ms exceeded).\n\nYour code may have an infinite loop or is taking too long to execute.",
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error during execution", e)
            CompilerResult(
                success = false,
                output = "",
                error = "Error: ${e.message ?: "Unknown error occurred"}\n\nMake sure you have an internet connection.",
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )
        }
    }

    /**
     * Execute code using Piston API
     */
    private fun executeWithPiston(
        code: String,
        className: String,
        startTime: Long,
        config: CompilerConfig
    ): CompilerResult {
        try {
            // Build request JSON
            val requestBody = JSONObject().apply {
                put("language", "java")
                put("version", JAVA_VERSION)
                put("files", JSONArray().apply {
                    put(JSONObject().apply {
                        put("name", "$className.java")
                        put("content", code)
                    })
                })
                // Optional: stdin if needed
                // put("stdin", "")
            }

            Log.d(TAG, "Sending request to Piston API for class: $className")

            // Make HTTP request
            val url = URL(PISTON_API_URL)
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                doOutput = true
                connectTimeout = 10000 // 10 seconds connect timeout
                readTimeout = 30000 // 30 seconds read timeout
            }

            // Send request
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            // Read response
            val responseCode = connection.responseCode
            val responseBody = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use(BufferedReader::readText)
            } else {
                connection.errorStream?.bufferedReader()?.use(BufferedReader::readText)
                    ?: "Unknown error (HTTP $responseCode)"
            }

            connection.disconnect()

            Log.d(TAG, "Piston API response code: $responseCode")

            if (responseCode != HttpURLConnection.HTTP_OK) {
                return CompilerResult(
                    success = false,
                    output = "",
                    error = "API Error (HTTP $responseCode): $responseBody",
                    executionTime = System.currentTimeMillis() - startTime,
                    compiledSuccessfully = false
                )
            }

            // Parse response
            return parseResponse(responseBody, startTime, config)

        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "No internet connection", e)
            return CompilerResult(
                success = false,
                output = "",
                error = "No internet connection.\n\nPlease check your network and try again.",
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Connection timeout", e)
            return CompilerResult(
                success = false,
                output = "",
                error = "Connection timeout.\n\nThe server took too long to respond. Please try again.",
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "API request failed", e)
            return CompilerResult(
                success = false,
                output = "",
                error = "Request failed: ${e.message}",
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )
        }
    }

    /**
     * Parse Piston API response
     */
    private fun parseResponse(
        responseBody: String,
        startTime: Long,
        config: CompilerConfig
    ): CompilerResult {
        try {
            val json = JSONObject(responseBody)

            // Check for compile stage
            val compile = json.optJSONObject("compile")
            val run = json.optJSONObject("run")

            // Check compilation errors first
            if (compile != null) {
                val compileStderr = compile.optString("stderr", "")
                val compileCode = compile.optInt("code", 0)

                if (compileCode != 0 || compileStderr.isNotEmpty()) {
                    Log.e(TAG, "Compilation failed: $compileStderr")
                    return CompilerResult(
                        success = false,
                        output = compile.optString("stdout", ""),
                        error = formatCompileError(compileStderr),
                        executionTime = System.currentTimeMillis() - startTime,
                        compiledSuccessfully = false
                    )
                }
            }

            // Check runtime results
            if (run != null) {
                val stdout = run.optString("stdout", "").trimEnd()
                val stderr = run.optString("stderr", "").trimEnd()
                val exitCode = run.optInt("code", 0)

                val executionTime = System.currentTimeMillis() - startTime

                // Runtime error
                if (exitCode != 0 || stderr.isNotEmpty()) {
                    Log.e(TAG, "Runtime error: $stderr")
                    return CompilerResult(
                        success = false,
                        output = stdout.take(config.maxOutputLength),
                        error = formatRuntimeError(stderr),
                        executionTime = executionTime,
                        compiledSuccessfully = true // Compiled but runtime error
                    )
                }

                // Success
                val finalOutput = if (stdout.isNotEmpty()) {
                    stdout
                } else {
                    "Code executed successfully (no output)"
                }

                Log.d(TAG, "Execution completed successfully in ${executionTime}ms")

                return CompilerResult(
                    success = true,
                    output = finalOutput.take(config.maxOutputLength),
                    error = null,
                    executionTime = executionTime,
                    compiledSuccessfully = true
                )
            }

            // No run object - unexpected response
            return CompilerResult(
                success = false,
                output = "",
                error = "Unexpected API response format",
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse response", e)
            return CompilerResult(
                success = false,
                output = "",
                error = "Failed to parse response: ${e.message}",
                executionTime = System.currentTimeMillis() - startTime,
                compiledSuccessfully = false
            )
        }
    }

    /**
     * Extract class name from Java code
     */
    private fun extractClassName(code: String): String? {
        // Match public class first (required for main method)
        val publicClassPattern = Regex("""public\s+class\s+(\w+)""")
        publicClassPattern.find(code)?.let {
            return it.groupValues[1]
        }

        // Match any class declaration
        val classPattern = Regex("""(?:public\s+|private\s+|protected\s+)?(?:final\s+|abstract\s+)?class\s+(\w+)""")
        return classPattern.find(code)?.groupValues?.get(1)
    }

    /**
     * Ensure code has proper Java structure
     * If user writes just statements, wrap them in a main method
     */
    private fun ensureProperStructure(code: String, className: String): String {
        val trimmedCode = code.trim()

        // Check if code already has a class definition
        if (trimmedCode.contains(Regex("""class\s+\w+"""))) {
            return trimmedCode
        }

        // Check if it looks like just statements (no class)
        // Wrap in a Main class with main method
        return """
public class Main {
    public static void main(String[] args) {
        $trimmedCode
    }
}
        """.trimIndent()
    }

    /**
     * Format compilation errors for better readability
     */
    private fun formatCompileError(error: String): String {
        if (error.isBlank()) return "Compilation Error: Unknown error"

        // Clean up the error message
        val cleanedError = error
            .replace(Regex("""/tmp/[^/]+/"""), "") // Remove temp paths
            .replace(Regex("""^\d+\s+errors?\s*$""", RegexOption.MULTILINE), "") // Remove "X errors" line
            .trim()

        return "Compilation Error:\n$cleanedError"
    }

    /**
     * Format runtime errors for better readability
     */
    private fun formatRuntimeError(error: String): String {
        if (error.isBlank()) return "Runtime Error: Unknown error"

        // Clean up stack traces
        val cleanedError = error
            .replace(Regex("""at java\.base/.*\n?"""), "") // Remove java.base frames
            .replace(Regex("""\s+at (?!Main\.).*\n?"""), "") // Keep only Main class frames
            .trim()

        return "Runtime Error:\n$cleanedError"
    }

    override fun getLanguageId(): String = "java"

    override fun getLanguageName(): String = "Java"

    override fun getFileExtension(): String = ".java"

    override fun validateSyntax(code: String): String? {
        // For remote compilation, we can't validate syntax locally
        // Return null (valid) and let the server handle errors
        return null
    }

    override fun supportsTestCases(): Boolean = true
}
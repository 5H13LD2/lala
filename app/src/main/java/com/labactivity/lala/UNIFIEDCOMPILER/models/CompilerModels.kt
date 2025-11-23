package com.labactivity.lala.UNIFIEDCOMPILER.models

/**
 * Unified data models for the compiler system
 */

/**
 * Represents the result of code execution/compilation
 */
data class CompilerResult(
    val success: Boolean,
    val output: String,
    val error: String? = null,
    val executionTime: Long = 0,
    val compiledSuccessfully: Boolean = true,
    val testCasesPassed: Int = 0,
    val totalTestCases: Int = 0,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Configuration for code execution
 */
data class CompilerConfig(
    val timeout: Long = 30000, // 30 seconds default
    val maxOutputLength: Int = 10000,
    val enableStdin: Boolean = false,
    val stdinInput: String = "",
    val testCases: List<TestCase> = emptyList()
)

/**
 * Test case for validation
 */
data class TestCase(
    val input: String = "",
    val expectedOutput: String,
    val description: String = ""
)

/**
 * Course compiler configuration from Firebase
 */
data class CourseCompilerInfo(
    val courseId: String = "",
    val courseName: String = "",
    val compilerType: String = "", // "python", "java", "sql", "kotlin", etc.
    val version: String = "",
    val supportedFeatures: List<String> = emptyList()
)

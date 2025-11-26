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
    // Compiler-related fields
    var courseId: String = "",
    var courseName: String = "",
    var compilerType: String = "",  // "python", "java", "sql", "kotlin", etc.
    var version: String = "",
    var supportedFeatures: List<String> = emptyList(),

    // Course metadata fields (these were missing - causing the warnings)
    var difficulty: String = "",
    var moduleCount: Int = 0,
    var name: String = "",
    var description: String = "",
    var category: String = "",
    var title: String = "",
    var hadCompiler: Boolean = false,
    var updatedAt: com.google.firebase.Timestamp? = null
)

package com.labactivity.lala.UNIFIEDCOMPILER

import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerConfig
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerResult

/**
 * Core interface that ALL language compilers must implement
 * This enables a unified, plug-and-play architecture
 */
interface CourseCompiler {

    /**
     * Compiles and/or executes the given code
     *
     * @param code The source code to compile/execute
     * @param config Optional configuration for execution
     * @return CompilerResult containing output, errors, and metadata
     */
    suspend fun compile(code: String, config: CompilerConfig = CompilerConfig()): CompilerResult

    /**
     * Returns the language identifier (e.g., "python", "java", "kotlin")
     */
    fun getLanguageId(): String

    /**
     * Returns the display name of the language
     */
    fun getLanguageName(): String

    /**
     * Validates code syntax before execution (optional)
     * Returns null if valid, error message if invalid
     */
    fun validateSyntax(code: String): String? = null

    /**
     * Returns whether this compiler supports test case validation
     */
    fun supportsTestCases(): Boolean = true

    /**
     * Returns the file extension for this language (e.g., ".py", ".java")
     */
    fun getFileExtension(): String
}

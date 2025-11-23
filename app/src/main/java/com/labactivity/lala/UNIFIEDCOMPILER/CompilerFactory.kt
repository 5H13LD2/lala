package com.labactivity.lala.UNIFIEDCOMPILER

import com.labactivity.lala.UNIFIEDCOMPILER.compilers.*
import android.content.Context

/**
 * COMPILER FACTORY - Central registry for all language compilers
 *
 * This is the CORE of the plug-and-play system.
 * To add a new language:
 * 1. Create a new class implementing CourseCompiler
 * 2. Register it in the registry map below
 * 3. That's it! The system will automatically use it.
 */
object CompilerFactory {

    private lateinit var applicationContext: Context
    private val registry = mutableMapOf<String, CourseCompiler>()

    /**
     * Initialize the factory with application context
     * Call this in Application onCreate() or MainActivity onCreate()
     */
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
        registerDefaultCompilers()
    }

    /**
     * Register all built-in compilers
     * This is called automatically during initialization
     */
    private fun registerDefaultCompilers() {
        // Register built-in compilers
        registry["python"] = PythonCompiler(applicationContext)
        registry["java"] = JavaCompiler()
        registry["kotlin"] = KotlinCompiler()

        // Future compilers can be added here
        // Example:
        // registry["sql"] = SQLExecutor(applicationContext)
        // registry["ruby"] = RubyCompiler()
        // registry["swift"] = SwiftCompiler()
        // registry["go"] = GoCompiler()
        // registry["javascript"] = JavaScriptCompiler()
        // registry["csharp"] = CSharpCompiler()
    }

    /**
     * Get a compiler for a specific language/course
     *
     * @param compilerType The language identifier (e.g., "python", "java", "kotlin")
     * @return CourseCompiler instance for the language
     * @throws IllegalArgumentException if compiler not found
     */
    fun getCompiler(compilerType: String): CourseCompiler {
        val normalizedType = compilerType.lowercase().trim()
        return registry[normalizedType]
            ?: throw IllegalArgumentException(
                "Compiler not found for '$compilerType'. " +
                "Supported languages: ${getSupportedLanguages().joinToString(", ")}"
            )
    }

    /**
     * Register a custom compiler dynamically
     * Useful for runtime plugin systems or custom courses
     */
    fun registerCompiler(languageId: String, compiler: CourseCompiler) {
        registry[languageId.lowercase()] = compiler
    }

    /**
     * Check if a compiler is registered
     */
    fun hasCompiler(compilerType: String): Boolean {
        return registry.containsKey(compilerType.lowercase().trim())
    }

    /**
     * Get all supported language IDs
     */
    fun getSupportedLanguages(): List<String> {
        return registry.keys.sorted()
    }

    /**
     * Get all registered compilers
     */
    fun getAllCompilers(): Map<String, CourseCompiler> {
        return registry.toMap()
    }

    /**
     * Unregister a compiler (useful for testing or dynamic unloading)
     */
    fun unregisterCompiler(languageId: String) {
        registry.remove(languageId.lowercase())
    }

    /**
     * Clear all compilers (useful for testing)
     */
    internal fun clearRegistry() {
        registry.clear()
    }
}

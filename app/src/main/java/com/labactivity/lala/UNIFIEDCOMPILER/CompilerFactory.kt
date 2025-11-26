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

    // Alias mapping for language variations
    private val aliases = mapOf(
        "python3" to "python",
        "py" to "python",
        "kt" to "kotlin"
    )

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
        registerDefaultCompilers()
    }

    private fun registerDefaultCompilers() {
        registry["python"] = PythonCompiler(applicationContext)
        registry["java"] = JavaCompiler()
        registry["kotlin"] = KotlinCompiler()
    }

    private fun resolveAlias(compilerType: String): String {
        val normalized = compilerType.trim().lowercase()
        return aliases[normalized] ?: normalized
    }

    fun getCompiler(compilerType: String): CourseCompiler {
        val normalizedType = compilerType.trim().lowercase()

        // Handle empty or blank input - default to Python
        if (normalizedType.isBlank()) {
            android.util.Log.w("CompilerFactory", "Empty compilerType received, defaulting to Python")
            return registry["python"]
                ?: throw IllegalArgumentException("Default compiler (python) not initialized")
        }

        val registryKey = resolveAlias(compilerType)

        return registry[registryKey]
            ?: throw IllegalArgumentException(
                "Compiler not found. Requested: '$compilerType' (resolved: '$registryKey'), " +
                        "Supported: ${getSupportedLanguages()}"
            )
    }

    fun hasCompiler(compilerType: String): Boolean {
        return registry.containsKey(resolveAlias(compilerType))
    }

    fun getSupportedLanguages(): List<String> {
        return registry.keys.sorted()
    }

    // ... rest of your methods
}
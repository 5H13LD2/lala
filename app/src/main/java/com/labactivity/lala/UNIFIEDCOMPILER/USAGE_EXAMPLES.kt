package com.labactivity.lala.UNIFIEDCOMPILER

import android.content.Context
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerConfig
import com.labactivity.lala.UNIFIEDCOMPILER.models.TestCase
import com.labactivity.lala.UNIFIEDCOMPILER.models.UnifiedChallenge
import com.labactivity.lala.UNIFIEDCOMPILER.services.CompilerService
import com.labactivity.lala.UNIFIEDCOMPILER.services.UnifiedAssessmentService
import com.labactivity.lala.UNIFIEDCOMPILER.ui.UnifiedCompilerActivity
import kotlinx.coroutines.launch

/**
 * ============================================================================
 * USAGE EXAMPLES FOR UNIFIED COMPILER SYSTEM
 * ============================================================================
 *
 * This file contains practical examples of how to use the unified compiler
 * system in different scenarios:
 *
 * 1. Running code directly with CompilerFactory
 * 2. Using CompilerService with Firebase courses
 * 3. Running technical assessments and challenges
 * 4. Launching the UnifiedCompilerActivity UI
 * 5. Integration with existing challenge systems
 *
 * ============================================================================
 */

// ============================================================================
// EXAMPLE 1: Direct Compiler Usage (Simple Case)
// ============================================================================

/**
 * Use this when you just want to run code for a specific language
 */
suspend fun example1_DirectCompilerUsage() {
    // Get compiler by language
    val pythonCompiler = CompilerFactory.getCompiler("python")

    // Execute code
    val result = pythonCompiler.compile("""
        print("Hello from Python!")
        for i in range(5):
            print(i)
    """.trimIndent())

    // Check result
    if (result.success) {
        println("Output: ${result.output}")
        println("Execution time: ${result.executionTime}ms")
    } else {
        println("Error: ${result.error}")
    }
}

// ============================================================================
// EXAMPLE 2: Using CompilerService with Course ID
// ============================================================================

/**
 * Use this when you have a courseId from Firebase and want automatic
 * compiler selection based on the course's compilerType
 */
suspend fun example2_CompilerServiceUsage(courseId: String) {
    val service = CompilerService()

    // The service automatically gets the right compiler for the course
    val result = service.executeCodeForCourse(
        courseId = courseId,
        code = """
            val name = "Kotlin"
            println("Hello from ${'$'}name!")
        """.trimIndent()
    )

    println("Result: ${result.output}")
}

// ============================================================================
// EXAMPLE 3: Running Technical Assessments
// ============================================================================

/**
 * Use this for technical assessments/challenges
 * This example shows how to fetch challenges and execute them
 */
suspend fun example3_TechnicalAssessment(courseId: String) {
    val assessmentService = UnifiedAssessmentService()

    // 1. Get all challenges for a course
    val challenges = assessmentService.getChallengesForCourse(courseId)

    println("Found ${challenges.size} challenges")

    // 2. Get user's challenges (with unlock logic)
    val userChallenges = assessmentService.getChallengesForUser()

    // 3. Execute a specific challenge
    val challenge = challenges.firstOrNull() ?: return

    val userCode = """
        print("My solution here")
    """

    val executionResult = assessmentService.executeChallenge(
        challengeId = challenge.id,
        userCode = userCode,
        challenge = challenge
    )

    // 4. Check results
    if (executionResult.passed) {
        println("✓ Challenge passed! Score: ${executionResult.score}%")

        // 5. Save progress
        assessmentService.saveProgress(
            challengeId = challenge.id,
            challenge = challenge,
            userCode = userCode,
            executionResult = executionResult
        )
    } else {
        println("✗ Challenge failed. Score: ${executionResult.score}%")
        println("Error: ${executionResult.compilerResult.error}")
    }
}

// ============================================================================
// EXAMPLE 4: Testing Code with Test Cases
// ============================================================================

/**
 * Use this when you need to validate code against test cases
 */
suspend fun example4_TestCaseValidation() {
    val compiler = CompilerFactory.getCompiler("java")

    val testCases = listOf(
        TestCase(
            input = "",
            expectedOutput = "Hello, World!",
            description = "Basic output test"
        ),
        TestCase(
            input = "5",
            expectedOutput = "25",
            description = "Square calculation test"
        )
    )

    val config = CompilerConfig(
        testCases = testCases,
        timeout = 10000
    )

    val code = """
        public class Test {
            public void run() {
                System.out.println("Hello, World!");
            }
        }
    """.trimIndent()

    val result = compiler.compile(code, config)

    println("Test Cases Passed: ${result.testCasesPassed}/${result.totalTestCases}")
    println("Score: ${(result.testCasesPassed * 100) / result.totalTestCases}%")
}

// ============================================================================
// EXAMPLE 5: Launching Universal Compiler Activity
// ============================================================================

/**
 * Use this to launch the compiler UI for a specific language or course
 */
fun example5_LaunchCompilerUI(context: Context) {
    // Method A: Launch with specific language
    val intent1 = Intent(context, UnifiedCompilerActivity::class.java).apply {
        putExtra(UnifiedCompilerActivity.EXTRA_LANGUAGE, "python")
        putExtra(UnifiedCompilerActivity.EXTRA_INITIAL_CODE, """
            print("Hello!")
        """.trimIndent())
    }
    context.startActivity(intent1)

    // Method B: Launch with course ID (auto-detects compiler)
    val intent2 = Intent(context, UnifiedCompilerActivity::class.java).apply {
        putExtra(UnifiedCompilerActivity.EXTRA_COURSE_ID, "python_beginner_course")
    }
    context.startActivity(intent2)
}

// ============================================================================
// EXAMPLE 6: Integration with Existing Challenge Activity
// ============================================================================

/**
 * Replace existing Java/Python challenge activities with unified approach
 */
class ExampleChallengeIntegration {

    private val assessmentService = UnifiedAssessmentService()

    /**
     * OLD WAY (separate helpers for each language):
     * - FirestoreJavaHelper for Java
     * - TechnicalAssessmentService for Python
     * - FirestoreSQLHelper for SQL
     *
     * NEW WAY (one service for all languages):
     */
    suspend fun loadChallengesNewWay(courseId: String) {
        // Single method works for Python, Java, Kotlin, SQL, etc.
        val challenges = assessmentService.getChallengesForCourse(courseId)

        challenges.forEach { challenge ->
            println("${challenge.title} (${challenge.compilerType}) - ${challenge.difficulty}")
        }
    }

    /**
     * OLD WAY (separate execution logic):
     * if (language == "java") {
     *     JavaRunner().executeJavaCode(code)
     * } else if (language == "python") {
     *     PythonCompiler().execute(code)
     * }
     *
     * NEW WAY (unified):
     */
    suspend fun executeChallengeNewWay(challenge: UnifiedChallenge, userCode: String) {
        // Works for ANY language automatically
        val result = assessmentService.executeChallenge(
            challengeId = challenge.id,
            userCode = userCode,
            challenge = challenge
        )

        // Save progress (works for all languages)
        if (result.passed) {
            assessmentService.saveProgress(challenge.id, challenge, userCode, result)
        }
    }
}

// ============================================================================
// EXAMPLE 7: Adding a New Language (Kotlin Course)
// ============================================================================

/**
 * SCENARIO: You want to add a Kotlin course in your CMS
 *
 * STEPS:
 */
class Example7_AddingNewLanguage {

    /**
     * STEP 1: Add course in Firebase
     *
     * Collection: courses
     * Document ID: kotlin_beginner_course
     *
     * {
     *   "courseId": "kotlin_beginner_course",
     *   "courseName": "Kotlin Beginner Course",
     *   "compilerType": "kotlin",  // <-- This is the key field
     *   "version": "1.9",
     *   "description": "Learn Kotlin from scratch"
     * }
     */

    /**
     * STEP 2: Add challenges in technical_assesment collection
     *
     * Collection: technical_assesment
     * Document: auto-generated ID
     *
     * {
     *   "courseId": "kotlin_beginner_course",  // <-- Links to course
     *   "title": "Hello Kotlin",
     *   "difficulty": "Easy",
     *   "brokenCode": "println(\"Fix me!\")",
     *   "correctOutput": "Hello, Kotlin!",
     *   "hint": "Use string interpolation"
     * }
     */

    /**
     * STEP 3: Use it! (No code changes needed)
     */
    suspend fun useNewKotlinCourse() {
        val service = UnifiedAssessmentService()

        // Automatically works with Kotlin compiler
        val challenges = service.getChallengesForCourse("kotlin_beginner_course")

        challenges.forEach { challenge ->
            println("Challenge: ${challenge.title}")
            println("Compiler: ${challenge.compilerType}") // Will be "kotlin"

            // Execute challenge
            val result = service.executeChallenge(
                challengeId = challenge.id,
                userCode = "println(\"Hello, Kotlin!\")",
                challenge = challenge
            )

            println("Passed: ${result.passed}")
        }
    }

    /**
     * That's it! The system automatically:
     * - Detects the compiler type from the course
     * - Uses KotlinCompiler to execute code
     * - Saves progress
     * - Awards XP
     *
     * NO NEED TO:
     * - Create a new helper class
     * - Modify existing activities
     * - Add special cases
     */
}

// ============================================================================
// EXAMPLE 8: Migrating from Old System
// ============================================================================

/**
 * How to migrate from old separate compiler system to unified system
 */
class Example8_Migration {

    /**
     * OLD CODE (BEFORE):
     */
    fun oldWay_ExecuteCode(language: String, code: String) {
        /*
        when (language) {
            "java" -> {
                val javaRunner = JavaRunner()
                val result = javaRunner.executeJavaCode(code)
                displayResult(result.output)
            }
            "python" -> {
                // Start Python compiler activity
                val intent = Intent(this, PythonCompilerActivity::class.java)
                intent.putExtra("code", code)
                startActivity(intent)
            }
            "sql" -> {
                val sqlHelper = FirestoreSQLHelper.getInstance()
                lifecycleScope.launch {
                    val challenges = sqlHelper.getAllChallenges()
                    // ...
                }
            }
        }
        */
    }

    /**
     * NEW CODE (AFTER):
     */
    suspend fun newWay_ExecuteCode(language: String, code: String) {
        // One line for ALL languages
        val compiler = CompilerFactory.getCompiler(language)
        val result = compiler.compile(code)
        displayResult(result.output)
    }

    /**
     * Or even simpler with CompilerService:
     */
    suspend fun newWay_ExecuteCodeWithCourse(courseId: String, code: String) {
        val service = CompilerService()
        val result = service.executeCodeForCourse(courseId, code)
        displayResult(result.output)
    }

    private fun displayResult(output: String) {
        println(output)
    }
}

// ============================================================================
// EXAMPLE 9: Checking Supported Languages
// ============================================================================

fun example9_CheckSupportedLanguages() {
    val service = CompilerService()

    // Get all supported languages
    val languages = service.getSupportedCompilers()
    println("Supported languages: ${languages.joinToString(", ")}")
    // Output: Supported languages: java, kotlin, python, sql

    // Check if specific language is supported
    if (service.isCompilerSupported("ruby")) {
        println("Ruby is supported!")
    } else {
        println("Ruby not available yet")
    }
}

// ============================================================================
// EXAMPLE 10: Advanced Configuration
// ============================================================================

suspend fun example10_AdvancedConfig() {
    val compiler = CompilerFactory.getCompiler("python")

    val config = CompilerConfig(
        timeout = 15000,              // 15 seconds max
        maxOutputLength = 5000,        // Limit output size
        enableStdin = true,            // Enable input
        stdinInput = "John\n25\n",    // Input data
        testCases = listOf(
            TestCase(
                input = "John\n25",
                expectedOutput = "Hello, John! You are 25 years old.",
                description = "Name and age test"
            )
        )
    )

    val code = """
        name = input("Name: ")
        age = input("Age: ")
        print(f"Hello, {name}! You are {age} years old.")
    """.trimIndent()

    val result = compiler.compile(code, config)

    println("Output: ${result.output}")
    println("Tests: ${result.testCasesPassed}/${result.totalTestCases}")
}

// ============================================================================
// SUMMARY
// ============================================================================

/**
 * KEY TAKEAWAYS:
 *
 * 1. ONE INTERFACE for all languages (CourseCompiler)
 * 2. ONE FACTORY to get compilers (CompilerFactory)
 * 3. ONE SERVICE for Firebase integration (CompilerService)
 * 4. ONE SERVICE for assessments (UnifiedAssessmentService)
 * 5. ONE ACTIVITY for UI (UnifiedCompilerActivity)
 *
 * NO MORE:
 * - Separate activities for Python/Java/SQL
 * - Separate helper classes
 * - If/else statements for language detection
 * - Duplicate code for each language
 *
 * TO ADD A NEW LANGUAGE:
 * 1. Create XYZCompiler class implementing CourseCompiler
 * 2. Register in CompilerFactory
 * 3. Add course in Firebase with compilerType: "xyz"
 * 4. DONE! Everything else works automatically.
 */

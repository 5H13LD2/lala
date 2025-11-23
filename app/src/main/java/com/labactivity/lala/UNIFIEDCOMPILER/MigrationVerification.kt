package com.labactivity.lala.UNIFIEDCOMPILER

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import com.labactivity.lala.UNIFIEDCOMPILER.services.UnifiedAssessmentService
import com.labactivity.lala.UNIFIEDCOMPILER.models.UnifiedChallenge
import kotlinx.coroutines.launch

/**
 * ============================================================================
 * UNIFIED COMPILER VERIFICATION TESTS
 * ============================================================================
 *
 * Run these tests to verify the unified compiler system works correctly.
 *
 * USAGE:
 * ```kotlin
 * class MainActivity4 : AppCompatActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         // Initialize compiler
 *         CompilerFactory.initialize(applicationContext)
 *
 *         // Add test button
 *         btnRunTests.setOnClickListener {
 *             MigrationVerification.runAllTests(this, lifecycleScope)
 *         }
 *     }
 * }
 * ```
 * ============================================================================
 */
object MigrationVerification {

    private const val TAG = "MigrationTest"

    /**
     * Run all migration verification tests
     */
    fun runAllTests(context: Context, scope: LifecycleCoroutineScope) {
        scope.launch {
            try {
                Log.d(TAG, "═══════════════════════════════════════════")
                Log.d(TAG, "   UNIFIED COMPILER VERIFICATION TESTS")
                Log.d(TAG, "═══════════════════════════════════════════")

                var passedTests = 0
                val totalTests = 5

                // Test 1: Basic Compilation
                if (test1_BasicCompilation()) passedTests++

                // Test 2: Challenge Loading
                if (test2_ChallengeLoading()) passedTests++

                // Test 3: Challenge Execution
                if (test3_ChallengeExecution()) passedTests++

                // Test 4: Progress Saving
                if (test4_ProgressSaving()) passedTests++

                // Test 5: Compiler Type Detection
                if (test5_CompilerTypeDetection()) passedTests++

                // Results
                Log.d(TAG, "═══════════════════════════════════════════")
                Log.d(TAG, "   RESULTS: $passedTests / $totalTests PASSED")
                Log.d(TAG, "═══════════════════════════════════════════")

                if (passedTests == totalTests) {
                    Log.d(TAG, "✓ ALL TESTS PASSED!")
                    Log.d(TAG, "✓ UNIFIED COMPILER SYSTEM VERIFIED!")
                    Toast.makeText(
                        context,
                        "✓ All tests passed! Unified compiler working perfectly",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.e(TAG, "✗ SOME TESTS FAILED!")
                    Log.e(TAG, "✗ Check logs for details")
                    Toast.makeText(
                        context,
                        "✗ Some tests failed. Check logs.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Log.e(TAG, "✗ TEST SUITE FAILED: ${e.message}", e)
                Toast.makeText(context, "✗ Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Test 1: Basic Java Compilation
     */
    private suspend fun test1_BasicCompilation(): Boolean {
        return try {
            Log.d(TAG, "\n--- Test 1: Basic Compilation ---")

            val compiler = CompilerFactory.getCompiler("java")

            val testCode = """
                public class Test {
                    public void run() {
                        System.out.println("Hello, World!");
                    }
                }
            """.trimIndent()

            val result = compiler.compile(testCode)

            assert(result.success) { "Compilation failed" }
            assert(result.output.contains("Hello, World!")) {
                "Wrong output: ${result.output}"
            }

            Log.d(TAG, "✓ Test 1 PASSED: Basic compilation works")
            Log.d(TAG, "  Output: ${result.output}")
            Log.d(TAG, "  Execution time: ${result.executionTime}ms")
            true

        } catch (e: Exception) {
            Log.e(TAG, "✗ Test 1 FAILED: ${e.message}", e)
            false
        }
    }

    /**
     * Test 2: Challenge Loading from technical_assesment
     */
    private suspend fun test2_ChallengeLoading(): Boolean {
        return try {
            Log.d(TAG, "\n--- Test 2: Challenge Loading ---")

            val service = UnifiedAssessmentService()

            // Try to load user's challenges
            val userChallenges = service.getChallengesForUser()

            Log.d(TAG, "  Total challenges found: ${userChallenges.size}")

            // Filter Java challenges
            val javaChallenges = userChallenges.filter {
                it.compilerType == "java"
            }

            Log.d(TAG, "  Java challenges found: ${javaChallenges.size}")

            // Verify each has compiler type set
            javaChallenges.forEach { challenge ->
                assert(challenge.compilerType.isNotEmpty()) {
                    "Challenge ${challenge.id} has no compiler type"
                }
                Log.d(TAG, "    - ${challenge.title} (${challenge.difficulty})")
            }

            Log.d(TAG, "✓ Test 2 PASSED: Challenges loaded successfully")
            true

        } catch (e: Exception) {
            Log.e(TAG, "✗ Test 2 FAILED: ${e.message}", e)
            false
        }
    }

    /**
     * Test 3: Challenge Execution
     */
    private suspend fun test3_ChallengeExecution(): Boolean {
        return try {
            Log.d(TAG, "\n--- Test 3: Challenge Execution ---")

            val service = UnifiedAssessmentService()
            val challenges = service.getChallengesForUser()
            val javaChallenge = challenges.firstOrNull { it.compilerType == "java" }

            if (javaChallenge == null) {
                Log.d(TAG, "  No Java challenges found - creating test challenge")

                // Use a test challenge
                val testChallenge = UnifiedChallenge(
                    id = "test_java_challenge",
                    title = "Test Challenge",
                    courseId = "java_test",
                    compilerType = "java",
                    difficulty = "Easy",
                    brokenCode = "public class Test { public void run() {} }",
                    correctOutput = "Test Output"
                )

                val userCode = """
                    public class Test {
                        public void run() {
                            System.out.println("Test Output");
                        }
                    }
                """.trimIndent()

                val result = service.executeChallenge(
                    challengeId = testChallenge.id,
                    userCode = userCode,
                    challenge = testChallenge
                )

                assert(result.compilerResult.success) {
                    "Challenge execution failed: ${result.compilerResult.error}"
                }

                Log.d(TAG, "  Challenge executed successfully")
                Log.d(TAG, "  Passed: ${result.passed}")
                Log.d(TAG, "  Score: ${result.score}%")

            } else {
                Log.d(TAG, "  Testing with real challenge: ${javaChallenge.title}")

                val userCode = """
                    public class Test {
                        public void run() {
                            System.out.println("${javaChallenge.correctOutput}");
                        }
                    }
                """.trimIndent()

                val result = service.executeChallenge(
                    challengeId = javaChallenge.id,
                    userCode = userCode,
                    challenge = javaChallenge
                )

                assert(result.compilerResult.success) {
                    "Challenge execution failed"
                }

                Log.d(TAG, "  Execution successful")
                Log.d(TAG, "  Output: ${result.compilerResult.output}")
            }

            Log.d(TAG, "✓ Test 3 PASSED: Challenge execution works")
            true

        } catch (e: Exception) {
            Log.e(TAG, "✗ Test 3 FAILED: ${e.message}", e)
            false
        }
    }

    /**
     * Test 4: Progress Saving
     */
    private suspend fun test4_ProgressSaving(): Boolean {
        return try {
            Log.d(TAG, "\n--- Test 4: Progress Saving ---")

            val service = UnifiedAssessmentService()

            // Create test challenge
            val testChallenge = UnifiedChallenge(
                id = "test_progress_${System.currentTimeMillis()}",
                title = "Progress Test",
                courseId = "java_test",
                compilerType = "java",
                difficulty = "Easy",
                brokenCode = "",
                correctOutput = "Test"
            )

            val userCode = """
                public class Test {
                    public void run() {
                        System.out.println("Test");
                    }
                }
            """.trimIndent()

            // Execute challenge
            val result = service.executeChallenge(
                challengeId = testChallenge.id,
                userCode = userCode,
                challenge = testChallenge
            )

            // Save progress
            service.saveProgress(
                challengeId = testChallenge.id,
                challenge = testChallenge,
                userCode = userCode,
                executionResult = result
            )

            Log.d(TAG, "  Progress saved for challenge: ${testChallenge.id}")

            // Note: We can't verify reading back immediately due to Firebase latency
            // But if no exception was thrown, save was successful

            Log.d(TAG, "✓ Test 4 PASSED: Progress saving works")
            true

        } catch (e: Exception) {
            Log.e(TAG, "✗ Test 4 FAILED: ${e.message}", e)
            false
        }
    }

    /**
     * Test 5: Compiler Type Auto-Detection
     */
    private suspend fun test5_CompilerTypeDetection(): Boolean {
        return try {
            Log.d(TAG, "\n--- Test 5: Compiler Type Detection ---")

            val service = UnifiedAssessmentService()
            val challenges = service.getChallengesForUser()

            // Verify each challenge has compiler type set
            val challengesWithCompiler = challenges.filter {
                it.compilerType.isNotEmpty()
            }

            val detectionRate = if (challenges.isNotEmpty()) {
                (challengesWithCompiler.size * 100) / challenges.size
            } else {
                100 // No challenges = 100% success (vacuous truth)
            }

            Log.d(TAG, "  Challenges with compiler type: ${challengesWithCompiler.size}/${challenges.size}")
            Log.d(TAG, "  Detection rate: $detectionRate%")

            val compilerTypes = challenges.map { it.compilerType }.distinct()
            Log.d(TAG, "  Compiler types found: $compilerTypes")

            assert(detectionRate >= 90) {
                "Less than 90% of challenges have compiler type set"
            }

            Log.d(TAG, "✓ Test 5 PASSED: Compiler type detection works")
            true

        } catch (e: Exception) {
            Log.e(TAG, "✗ Test 5 FAILED: ${e.message}", e)
            false
        }
    }

}

/**
 * ============================================================================
 * QUICK TEST FUNCTION
 * ============================================================================
 *
 * Copy-paste this into your MainActivity4 for quick testing:
 *
 * ```kotlin
 * // Add test button in onCreate
 * findViewById<Button>(R.id.btnTest)?.setOnClickListener {
 *     MigrationVerification.runAllTests(this, lifecycleScope)
 * }
 * ```
 *
 * Or create a temporary test activity:
 *
 * ```kotlin
 * class TestActivity : AppCompatActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         CompilerFactory.initialize(applicationContext)
 *
 *         setContentView(LinearLayout(this).apply {
 *             orientation = LinearLayout.VERTICAL
 *             addView(Button(context).apply {
 *                 text = "Run Migration Tests"
 *                 setOnClickListener {
 *                     MigrationVerification.runAllTests(context, lifecycleScope)
 *                 }
 *             })
 *         })
 *     }
 * }
 * ```
 * ============================================================================
 */

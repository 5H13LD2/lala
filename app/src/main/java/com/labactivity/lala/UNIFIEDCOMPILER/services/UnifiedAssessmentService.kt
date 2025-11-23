package com.labactivity.lala.UNIFIEDCOMPILER.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.Timestamp
import com.labactivity.lala.GAMIFICATION.XPManager
import com.labactivity.lala.UNIFIEDCOMPILER.CompilerFactory
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerConfig
import com.labactivity.lala.UNIFIEDCOMPILER.models.CompilerResult
import com.labactivity.lala.UNIFIEDCOMPILER.models.TestCase
import com.labactivity.lala.UNIFIEDCOMPILER.models.UnifiedChallenge
import com.labactivity.lala.UNIFIEDCOMPILER.models.UnifiedChallengeProgress
import com.labactivity.lala.UNIFIEDCOMPILER.models.ChallengeExecutionResult
import kotlinx.coroutines.tasks.await


/**
 * UNIFIED ASSESSMENT SERVICE
 *
 * This service connects the Unified Compiler System with the technical_assessment collection.
 * It handles:
 * - Fetching challenges from technical_assessment
 * - Executing challenges using the appropriate compiler based on courseId
 * - Saving progress
 * - Awarding XP
 *
 * This replaces the need for separate JavaHelper, SQLHelper, etc.
 */
class UnifiedAssessmentService {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val xpManager = XPManager()
    private val compilerService = CompilerService()

    companion object {
        private const val COLLECTION_TECHNICAL_ASSESSMENT = "technical_assesment"
        private const val COLLECTION_USER_PROGRESS = "user_progress"
        private const val SUB_COLLECTION_ASSESSMENT_PROGRESS = "technical_assessment_progress"
    }

    /**
     * Get all challenges for a specific course
     * Uses compilerType field from Firebase (each challenge specifies its own language)
     */
    suspend fun getChallengesForCourse(courseId: String): List<UnifiedChallenge> {
        return try {
            // Fetch challenges for this course
            val snapshot = firestore.collection(COLLECTION_TECHNICAL_ASSESSMENT)
                .whereEqualTo("courseId", courseId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(UnifiedChallenge::class.java)?.copy(
                    id = doc.id
                    // compilerType is now read directly from Firebase document
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get all challenges for enrolled courses
     * With unlock logic based on difficulty
     */
    suspend fun getChallengesForUser(): List<UnifiedChallenge> {
        val userId = auth.currentUser?.uid ?: return emptyList()

        try {
            // Get user's enrolled courses
            val enrolledCourseIds = getUserEnrolledCourseIds(userId)

            if (enrolledCourseIds.isEmpty()) return emptyList()

            // Fetch challenges in batches (Firestore limit: 10 items per 'in' query)
            val allChallenges = mutableListOf<UnifiedChallenge>()

            enrolledCourseIds.chunked(10).forEach { batch ->
                val snapshot = firestore.collection(COLLECTION_TECHNICAL_ASSESSMENT)
                    .whereIn("courseId", batch)
                    .get()
                    .await()

                val challenges = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(UnifiedChallenge::class.java)?.copy(id = doc.id)
                }

                allChallenges.addAll(challenges)
            }

            // compilerType is now read directly from Firebase document
            // No need to derive from courseInfo

            // Apply unlock logic
            return applyUnlockLogic(allChallenges, userId)

        } catch (e: Exception) {
            return emptyList()
        }
    }

    /**
     * Execute a challenge using the unified compiler
     */
    suspend fun executeChallenge(
        challengeId: String,
        userCode: String,
        challenge: UnifiedChallenge
    ): ChallengeExecutionResult {
        val startTime = System.currentTimeMillis()

        try {
            // Get the appropriate compiler
            val compiler = CompilerFactory.getCompiler(challenge.compilerType)

            // Build test cases from challenge
            val testCases = buildTestCases(challenge)

            // Configure execution
            val config = CompilerConfig(
                timeout = 30000,
                maxOutputLength = 10000,
                testCases = testCases
            )

            // Execute code
            val result = compiler.compile(userCode, config)

            // Calculate score
            val score = if (result.totalTestCases > 0) {
                (result.testCasesPassed * 100) / result.totalTestCases
            } else {
                if (result.success) 100 else 0
            }

            val passed = score >= 70 // Passing grade: 70%

            return ChallengeExecutionResult(
                compilerResult = result,
                score = score,
                passed = passed,
                executionTime = System.currentTimeMillis() - startTime,
                testCasesPassed = result.testCasesPassed,
                totalTestCases = result.totalTestCases
            )

        } catch (e: Exception) {
            return ChallengeExecutionResult(
                compilerResult = CompilerResult(
                    success = false,
                    output = "",
                    error = "Execution failed: ${e.message}",
                    executionTime = System.currentTimeMillis() - startTime
                ),
                score = 0,
                passed = false,
                executionTime = System.currentTimeMillis() - startTime
            )
        }
    }

    /**
     * Save user progress after challenge attempt
     */
    suspend fun saveProgress(
        challengeId: String,
        challenge: UnifiedChallenge,
        userCode: String,
        executionResult: ChallengeExecutionResult
    ) {
        val userId = auth.currentUser?.uid ?: return

        try {
            val progressData = hashMapOf(
                "challengeId" to challengeId,
                "challengeTitle" to challenge.title,
                "status" to if (executionResult.passed) "completed" else "in_progress",
                "attempts" to FieldValue.increment(1),
                "bestScore" to executionResult.score,
                "lastAttemptDate" to Timestamp.now(),
                "timeTaken" to executionResult.executionTime,
                "userCode" to userCode,
                "passed" to executionResult.passed,
                "updatedAt" to Timestamp.now(),
                "compilerType" to challenge.compilerType // Track which compiler was used
            )

            firestore.collection(COLLECTION_USER_PROGRESS)
                .document(userId)
                .collection(SUB_COLLECTION_ASSESSMENT_PROGRESS)
                .document(challengeId)
                .set(progressData)
                .await()

            // Award XP if passed
            if (executionResult.passed) {
                xpManager.awardTechnicalAssessmentXP(userId, executionResult.passed)
            }

        } catch (e: Exception) {
            // Handle error
        }
    }

    /**
     * Get user progress for a specific challenge
     */
    suspend fun getUserProgress(challengeId: String): UnifiedChallengeProgress? {
        val userId = auth.currentUser?.uid ?: return null

        return try {
            val doc = firestore.collection(COLLECTION_USER_PROGRESS)
                .document(userId)
                .collection(SUB_COLLECTION_ASSESSMENT_PROGRESS)
                .document(challengeId)
                .get()
                .await()

            doc.toObject(UnifiedChallengeProgress::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get all user progress
     */
    suspend fun getAllUserProgress(): Map<String, UnifiedChallengeProgress> {
        val userId = auth.currentUser?.uid ?: return emptyMap()

        return try {
            val snapshot = firestore.collection(COLLECTION_USER_PROGRESS)
                .document(userId)
                .collection(SUB_COLLECTION_ASSESSMENT_PROGRESS)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(UnifiedChallengeProgress::class.java)?.let { progress ->
                    doc.id to progress
                }
            }.toMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Build test cases from challenge data
     */
    private fun buildTestCases(challenge: UnifiedChallenge): List<TestCase> {
        // If challenge has expected output, create test case
        return if (challenge.correctOutput.isNotEmpty()) {
            listOf(
                TestCase(
                    input = "",
                    expectedOutput = challenge.correctOutput,
                    description = "Challenge validation"
                )
            )
        } else {
            emptyList()
        }
    }

    /**
     * Get enrolled course IDs for user
     */
    private suspend fun getUserEnrolledCourseIds(userId: String): List<String> {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val courseTaken = doc.get("courseTaken") as? List<*> ?: emptyList<Any>()

            courseTaken.mapNotNull { course ->
                when (course) {
                    is String -> course
                    is Map<*, *> -> course["courseId"] as? String
                    else -> null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Apply unlock logic based on difficulty progression
     */
    private suspend fun applyUnlockLogic(
        challenges: List<UnifiedChallenge>,
        userId: String
    ): List<UnifiedChallenge> {
        val userProgress = getAllUserProgress()

        // Count completed challenges by difficulty
        val completedByDifficulty = challenges
            .filter { userProgress[it.id]?.passed == true }
            .groupBy { it.difficulty }
            .mapValues { it.value.size }

        val easyCount = challenges.count { it.difficulty.equals("Easy", ignoreCase = true) }
        val completedEasy = completedByDifficulty["Easy"] ?: 0

        val mediumCount = challenges.count { it.difficulty.equals("Medium", ignoreCase = true) }
        val completedMedium = completedByDifficulty["Medium"] ?: 0

        return challenges.map { challenge ->
            val isUnlocked = when (challenge.difficulty.lowercase()) {
                "easy" -> true // Always unlocked
                "medium" -> completedEasy >= easyCount // Unlock when all Easy done
                "hard" -> (completedEasy >= easyCount) && (completedMedium >= mediumCount) // Unlock when Easy + Medium done
                else -> true
            }

            challenge.copy(isUnlocked = isUnlocked)
        }
    }
}
